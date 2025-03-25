package com.ginkgooai.core.project.service.application;

import com.ginkgooai.core.common.exception.ResourceNotFoundException;
import com.ginkgooai.core.common.utils.ContextUtils;
import com.ginkgooai.core.project.client.identity.IdentityClient;
import com.ginkgooai.core.project.client.identity.dto.UserInfo;
import com.ginkgooai.core.project.client.storage.StorageClient;
import com.ginkgooai.core.project.client.storage.dto.CloudFileResponse;
import com.ginkgooai.core.project.domain.application.Application;
import com.ginkgooai.core.project.domain.application.ShortlistItem;
import com.ginkgooai.core.project.domain.application.Submission;
import com.ginkgooai.core.project.domain.application.SubmissionComment;
import com.ginkgooai.core.project.dto.request.CommentCreateRequest;
import com.ginkgooai.core.project.dto.request.SubmissionCreateRequest;
import com.ginkgooai.core.project.dto.response.SubmissionCommentResponse;
import com.ginkgooai.core.project.dto.response.SubmissionResponse;
import com.ginkgooai.core.project.repository.ApplicationRepository;
import com.ginkgooai.core.project.repository.ShortlistItemRepository;
import com.ginkgooai.core.project.repository.SubmissionCommentRepository;
import com.ginkgooai.core.project.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.ginkgooai.core.common.constant.ContextsConstant.USER_ID;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubmissionService {
    private final ApplicationRepository applicationRepository;

    private final SubmissionRepository submissionRepository;
    
    private final ShortlistItemRepository shortlistItemRepository;

    private final SubmissionCommentRepository submissionCommentRepository;

    private final StorageClient storageClient;
    
    private final IdentityClient identityClient;

    /**
     * Creates a new submission in the specified workspace using the provided submission details.
     *
     * <p>The method retrieves the application associated with the submission and fetches the video file's
     * metadata. If the application or video file is not found, a ResourceNotFoundException is thrown.
     * It then constructs and saves a new submission, returning a corresponding SubmissionResponse.
     *
     * @param workspaceId the workspace identifier where the submission is created
     * @param request the submission creation details, including application and video identifiers
     * @param userId the identifier of the user creating the submission
     * @return a SubmissionResponse representing the newly created submission
     * @throws ResourceNotFoundException if the application or video file cannot be found
     */
    @Transactional
    public SubmissionResponse createSubmission(String workspaceId, 
                                               SubmissionCreateRequest request, 
                                               String userId) {

        Application application = applicationRepository.findById(request.getApplicationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Application", "id", request.getApplicationId()));

        List<CloudFileResponse> videoFiles = storageClient.getFileDetails(Arrays.asList(request.getVideoId())).getBody();
        log.debug("Video files: {}", videoFiles);

        CloudFileResponse video = videoFiles.stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Video file", "id", request.getVideoId()));
        Submission submission = Submission.builder()
                .workspaceId(workspaceId)
                .application(application)
                .videoName(video.getOriginalName())
                .videoUrl(video.getStoragePath())
                .videoDuration(video.getVideoDuration())
                .videoThumbnailUrl(video.getVideoThumbnailUrl())
                .videoResolution(video.getVideoResolution())
                .mimeType(video.getFileType())
                .createdBy(userId)
                .build();
        Submission savedSubmission = submissionRepository.save(submission);

        return SubmissionResponse.from(savedSubmission, Collections.EMPTY_LIST, userId);
    }

    /**
     * Retrieves a submission by its unique identifier.
     *
     * <p>This method locates the submission using the provided ID and converts it into a
     * SubmissionResponse. The response includes the submission details along with the current
     * user identifier fetched from the application context. A ResourceNotFoundException is thrown
     * if the submission does not exist.</p>
     *
     * @param submissionId the unique identifier of the submission to retrieve
     * @return a SubmissionResponse representing the submission details
     */
    @Transactional(readOnly = true)
    public SubmissionResponse getSubmission(String submissionId) {
        Submission submission = findSubmissionById(submissionId);
        return SubmissionResponse.from(submission, Collections.EMPTY_LIST, ContextUtils.get(USER_ID, String.class, null));
    }

    /**
     * Deletes the specified submission and cleans up associated shortlist item references.
     *
     * <p>This method retrieves the submission by its ID and verifies that the requesting user
     * is the creator. If the user is not authorized, an AccessDeniedException is thrown.
     * It then removes the submission from any related shortlist items—deleting a shortlist item if it
     * no longer contains any submissions, or updating it otherwise—before deleting the submission.
     * </p>
     *
     * @param submissionId the identifier of the submission to delete
     * @param userId the identifier of the user performing the deletion
     *
     * @throws AccessDeniedException if the user is not authorized to delete the submission
     */
    @Transactional
    public void deleteSubmission(String submissionId, String userId) {
        Submission submission = findSubmissionById(submissionId);

        if (!submission.getCreatedBy().equals(userId)) {
            throw new AccessDeniedException("Not authorized to delete this submission");
        }

        // 删除相关的 shortlist items 引用
        List<ShortlistItem> shortlistItems = shortlistItemRepository.findAllBySubmissionId(submissionId, userId);
        for (ShortlistItem item : shortlistItems) {
            item.getSubmissions().remove(submission);
            if (item.getSubmissions().isEmpty()) {
                shortlistItemRepository.delete(item);
            } else {
                shortlistItemRepository.save(item);
            }
        }

        submissionRepository.delete(submission);

        log.info("Deleted submission: {}", submissionId);
    }

    /**
     * Adds a new comment to the specified submission and returns an updated submission response.
     * <p>
     * The method creates a comment from the provided request. If a parent comment ID is specified,
     * it retrieves the corresponding parent comment and throws a ResourceNotFoundException if not found.
     * The updated submission is then saved, and user details for all distinct comment authors are aggregated
     * into the response.
     * </p>
     *
     * @param submissionId the ID of the submission to which the comment is added
     * @param workspaceId the workspace identifier associated with the comment
     * @param request the request containing the comment's details, including content, type, and an optional parent comment ID
     * @param userId the ID of the user creating the comment
     * @return a SubmissionResponse representing the updated submission and associated user information
     * @throws ResourceNotFoundException if the specified parent comment is not found
     */
    public SubmissionResponse addComment(String submissionId, String workspaceId,
                                         CommentCreateRequest request, String userId) {
        Submission submission = findSubmissionById(submissionId);
        SubmissionComment comment = SubmissionComment.builder()
                .submission(submission)
                .content(request.getContent())
                .type(request.getType())
                .workspaceId(workspaceId)
                .createdBy(userId)
                .build();

        if (request.getParentCommentId() != null) {
            SubmissionComment parentComment = submissionCommentRepository
                    .findById(request.getParentCommentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent comment", "id", request.getParentCommentId()));
            comment.setParentComment(parentComment);
        }

        submission.getComments().add(comment);
        submissionRepository.save(submission);

        List<UserInfo> users = identityClient.getUsersByIds(submission.getComments().stream().map(SubmissionComment::getCreatedBy).distinct().toList()).getBody();

        return SubmissionResponse.from(submission, users, ContextUtils.get(USER_ID, String.class, null));
    }

    /**
     * Deletes a comment from a submission if the specified user is the comment's creator.
     *
     * <p>This method retrieves the submission by its ID and removes the comment identified by
     * {@code commentId} only when it was created by the user specified by {@code userId}. The updated
     * submission is then persisted.</p>
     *
     * @param submissionId the identifier of the submission containing the comment
     * @param commentId the identifier of the comment to delete
     * @param userId the identifier of the user attempting to delete the comment
     * @throws ResourceNotFoundException if no submission exists with the given submissionId
     */
    @Transactional
    public void deleteComment(String submissionId, String commentId, String userId) {
        Submission submission = findSubmissionById(submissionId);

        submission.getComments().removeIf(comment ->
                comment.getId().equals(commentId) && comment.getCreatedBy().equals(userId)
        );

        submissionRepository.save(submission);
    }

    /**
     * Retrieves all comments for the specified submission as a sorted list of comment responses.
     *
     * <p>This method fetches the submission corresponding to the provided ID, retrieves user information for
     * each comment via the identity client, and assembles the comments into response objects. The resulting
     * list is sorted by the creation date of each comment in ascending order.</p>
     *
     * @param submissionId the unique identifier of the submission
     * @return a list of {@code SubmissionCommentResponse} objects sorted by comment creation date
     * @throws ResourceNotFoundException if no submission exists for the provided ID
     */
    @Transactional(readOnly = true)
    public List<SubmissionCommentResponse> listComments(String submissionId) {
        Submission submission = findSubmissionById(submissionId);

        Map<String, UserInfo> usersMap = identityClient.getUsersByIds(submission.getComments().stream().map(SubmissionComment::getCreatedBy).distinct().toList())
                .getBody().stream().collect(Collectors.toMap(UserInfo::getId, user -> user));

        return submission.getComments().stream()
                .sorted(Comparator.comparing(SubmissionComment::getCreatedAt))
                .map(t -> SubmissionCommentResponse.from(t, usersMap.get(t.getCreatedBy())))
                .collect(Collectors.toList());
    }

    private Submission findSubmissionById(String id) {
        String workspaceId = ContextUtils.getWorkspaceId();

        return submissionRepository.findOne(
                (root, query, cb) -> cb.and(
                        cb.equal(root.get("id"), id),
                        cb.equal(root.get("workspaceId"), workspaceId)
                )
        ).orElseThrow(() -> new ResourceNotFoundException("Submission", "id", id));
    }
}