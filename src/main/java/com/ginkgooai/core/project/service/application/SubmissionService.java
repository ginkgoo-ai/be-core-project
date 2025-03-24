package com.ginkgooai.core.project.service.application;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ginkgooai.core.common.bean.ActivityType;
import static com.ginkgooai.core.common.constant.ContextsConstant.USER_ID;
import com.ginkgooai.core.common.exception.ResourceNotFoundException;
import com.ginkgooai.core.common.utils.ContextUtils;
import com.ginkgooai.core.project.client.identity.IdentityClient;
import com.ginkgooai.core.project.client.identity.dto.UserInfoResponse;
import com.ginkgooai.core.project.client.storage.StorageClient;
import com.ginkgooai.core.project.client.storage.dto.CloudFileResponse;
import com.ginkgooai.core.project.domain.application.Application;
import com.ginkgooai.core.project.domain.application.CommentType;
import com.ginkgooai.core.project.domain.application.ShortlistItem;
import com.ginkgooai.core.project.domain.application.Submission;
import com.ginkgooai.core.project.domain.application.SubmissionComment;
import com.ginkgooai.core.project.domain.application.SubmissionViewRecord;
import com.ginkgooai.core.project.dto.request.CommentCreateRequest;
import com.ginkgooai.core.project.dto.request.SubmissionCreateRequest;
import com.ginkgooai.core.project.dto.response.SubmissionCommentResponse;
import com.ginkgooai.core.project.dto.response.SubmissionResponse;
import com.ginkgooai.core.project.repository.ApplicationRepository;
import com.ginkgooai.core.project.repository.ShortlistItemRepository;
import com.ginkgooai.core.project.repository.SubmissionCommentRepository;
import com.ginkgooai.core.project.repository.SubmissionRepository;
import com.ginkgooai.core.project.repository.SubmissionViewRecordRepository;
import com.ginkgooai.core.project.service.ActivityLoggerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubmissionService {
    private final ApplicationRepository applicationRepository;

    private final SubmissionRepository submissionRepository;

    private final ShortlistItemRepository shortlistItemRepository;

    private final SubmissionCommentRepository submissionCommentRepository;

    private final SubmissionViewRecordRepository viewRecordRepository;

    private final StorageClient storageClient;

    private final IdentityClient identityClient;

    private final ActivityLoggerService activityLogger;

    @Transactional
    public SubmissionResponse createSubmission(String workspaceId,
            SubmissionCreateRequest request,
            String userId) {

        Application application = applicationRepository.findById(request.getApplicationId())
                .orElseThrow(() -> new ResourceNotFoundException("Application", "id", request.getApplicationId()));

        List<CloudFileResponse> videoFiles = storageClient.getFileDetails(Arrays.asList(request.getVideoId()))
                .getBody();
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

        activityLogger.log(
                workspaceId,
                application.getProject().getId(),
                application.getId(),
                ActivityType.SUBMISSION_ADDED_TO_SHORTLIST,
                Map.of(
                        "user", userId,
                        "talentName", application.getTalent().getName(),
                        "videoName", submission.getVideoName()),
                null,
                userId);

        return SubmissionResponse.from(savedSubmission, Collections.EMPTY_LIST, userId);
    }

    @Transactional(readOnly = true)
    public SubmissionResponse getSubmission(String submissionId) {
        Submission submission = findSubmissionById(submissionId);

        List<UserInfoResponse> users = identityClient
                .getUsersByIds(
                        submission.getComments().stream().map(SubmissionComment::getCreatedBy).distinct().toList())
                .getBody();
        return SubmissionResponse.from(submission, users, ContextUtils.get(USER_ID, String.class, null));
    }

    @Transactional
    public void deleteSubmission(String submissionId, String userId) {
        Submission submission = findSubmissionById(submissionId);

        if (!submission.getCreatedBy().equals(userId)) {
            throw new AccessDeniedException("Not authorized to delete this submission");
        }

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
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Parent comment", "id", request.getParentCommentId()));
            comment.setParentComment(parentComment);
        }

        submission.getComments().add(comment);
        submissionRepository.save(submission);

        if (comment.getType() == CommentType.PUBLIC) {
            return SubmissionResponse.from(submission, ContextUtils.getUserId());
        }

        List<UserInfoResponse> users = identityClient
                .getUsersByIds(
                        submission.getComments().stream().map(SubmissionComment::getCreatedBy).distinct().toList())
                .getBody();
        return SubmissionResponse.from(submission, users, ContextUtils.getUserId());
    }

    @Transactional
    public void deleteComment(String submissionId, String commentId, String userId) {
        Submission submission = findSubmissionById(submissionId);

        submission.getComments()
                .removeIf(comment -> comment.getId().equals(commentId) && comment.getCreatedBy().equals(userId));

        submissionRepository.save(submission);
    }

    @Transactional(readOnly = true)
    public List<SubmissionCommentResponse> listComments(String submissionId) {
        Submission submission = findSubmissionById(submissionId);

        Map<String, UserInfoResponse> usersMap = identityClient
                .getUsersByIds(
                        submission.getComments().stream().map(SubmissionComment::getCreatedBy).distinct().toList())
                .getBody().stream().collect(Collectors.toMap(UserInfoResponse::getId, user -> user));

        return submission.getComments().stream()
                .sorted(Comparator.comparing(SubmissionComment::getCreatedAt))
                .map(t -> SubmissionCommentResponse.from(t, usersMap.get(t.getCreatedBy())))
                .collect(Collectors.toList());
    }

    @Transactional
    public void incrementViewCount(String submissionId, String userId, String ipAddress) {
        Submission submission = findSubmissionById(submissionId);

        if (shouldCountView(submission, userId, ipAddress)) {
            SubmissionViewRecord viewRecord = SubmissionViewRecord.builder()
                    .submission(submission)
                    .workspaceId(submission.getWorkspaceId())
                    .userId(userId)
                    .ipAddress(ipAddress)
                    .build();

            viewRecordRepository.save(viewRecord);

            // Atomically increment view count
            if (submission.getViewCount() == null) {
                submission.setViewCount(1L);
            } else {
                submission.setViewCount(submission.getViewCount() + 1);
            }

            submissionRepository.save(submission);
            log.debug("Recorded video view: submissionId={}, newCount={}, user={}, ip={}",
                    submissionId, submission.getViewCount(), userId, ipAddress);
        } else {
            log.debug("Skipped view count: submissionId={}, user={}, ip={} (already viewed)",
                    submissionId, userId, ipAddress);
        }
    }

    /**
     * Determines if a view should be counted.
     * Simple deduplication logic - each user only counts once per video
     */
    private boolean shouldCountView(Submission submission, String userId, String ipAddress) {
        // If both are empty, default to count
        if ((userId == null || userId.isEmpty()) && (ipAddress == null || ipAddress.isEmpty())) {
            return true;
        }

        // Check if this user/IP has already viewed this submission
        boolean hasExistingView = viewRecordRepository.existsBySubmissionIdAndUserIdOrIpAddress(
                submission.getId(), userId);

        // Only count if no previous view record exists
        return !hasExistingView;
    }

    private Submission findSubmissionById(String id) {
        String workspaceId = ContextUtils.getWorkspaceId();

        return submissionRepository.findOne(
                (root, query, cb) -> cb.and(
                        cb.equal(root.get("id"), id),
                        cb.equal(root.get("workspaceId"), workspaceId)))
                .orElseThrow(() -> new ResourceNotFoundException("Submission", "id", id));
    }
}