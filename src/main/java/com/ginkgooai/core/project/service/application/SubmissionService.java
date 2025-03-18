package com.ginkgooai.core.project.service.application;

import com.ginkgooai.core.common.exception.ResourceNotFoundException;
import com.ginkgooai.core.common.utils.ContextUtils;
import com.ginkgooai.core.project.client.storage.StorageClient;
import com.ginkgooai.core.project.client.storage.dto.CloudFileResponse;
import com.ginkgooai.core.project.domain.application.Application;
import com.ginkgooai.core.project.domain.application.Submission;
import com.ginkgooai.core.project.domain.application.SubmissionComment;
import com.ginkgooai.core.project.dto.request.CommentCreateRequest;
import com.ginkgooai.core.project.dto.request.SubmissionCreateRequest;
import com.ginkgooai.core.project.dto.response.SubmissionCommentResponse;
import com.ginkgooai.core.project.dto.response.SubmissionResponse;
import com.ginkgooai.core.project.repository.ApplicationRepository;
import com.ginkgooai.core.project.repository.SubmissionCommentRepository;
import com.ginkgooai.core.project.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.ginkgooai.core.common.constant.ContextsConstant.USER_ID;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubmissionService {
    private final ApplicationRepository applicationRepository;

    private final SubmissionRepository submissionRepository;

    private final SubmissionCommentRepository submissionCommentRepository;

    private final StorageClient storageClient;

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

        return SubmissionResponse.from(savedSubmission, userId);
    }

    @Transactional(readOnly = true)
    public SubmissionResponse getSubmission(String submissionId) {
        Submission submission = findSubmissionById(submissionId);
        return SubmissionResponse.from(submission, ContextUtils.get(USER_ID, String.class, null));
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
                    .orElseThrow(() -> new ResourceNotFoundException("Parent comment", "id", request.getParentCommentId()));
            comment.setParentComment(parentComment);
        }

        submission.getComments().add(comment);
        submissionRepository.save(submission);

        return SubmissionResponse.from(submission, ContextUtils.get(USER_ID, String.class, null));
    }

    @Transactional
    public void deleteComment(String submissionId, String commentId, String userId) {
        Submission submission = findSubmissionById(submissionId);

        submission.getComments().removeIf(comment ->
                comment.getId().equals(commentId) && comment.getCreatedBy().equals(userId)
        );

        submissionRepository.save(submission);
    }

    @Transactional(readOnly = true)
    public List<SubmissionCommentResponse> listComments(String submissionId) {
        Submission submission = findSubmissionById(submissionId);

        return submission.getComments().stream()
                .sorted(Comparator.comparing(SubmissionComment::getCreatedAt))
                .map(SubmissionCommentResponse::from)
                .collect(Collectors.toList());
    }

    private Submission findSubmissionById(String id) {
        String workspaceId = ContextUtils.get().getWorkspaceId();

        return submissionRepository.findOne(
                (root, query, cb) -> cb.and(
                        cb.equal(root.get("id"), id),
                        cb.equal(root.get("workspaceId"), workspaceId)
                )
        ).orElseThrow(() -> new ResourceNotFoundException("Submission", "id", id));
    }
}