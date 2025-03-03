package com.ginkgooai.core.project.service.application;

import com.ginkgooai.core.common.context.WorkspaceContext;
import com.ginkgooai.core.common.exception.ResourceNotFoundException;
import com.ginkgooai.core.project.domain.application.Application;
import com.ginkgooai.core.project.domain.application.Submission;
import com.ginkgooai.core.project.domain.application.SubmissionComment;
import com.ginkgooai.core.project.dto.request.SubmissionCreateRequest;
import com.ginkgooai.core.project.dto.response.SubmissionCommentResponse;
import com.ginkgooai.core.project.dto.response.SubmissionResponse;
import com.ginkgooai.core.project.repository.ApplicationRepository;
import com.ginkgooai.core.project.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubmissionService {
    private final ApplicationRepository applicationRepository;
    
    private final SubmissionRepository submissionRepository;

    @Transactional
    public SubmissionResponse createSubmission(String workspaceId, String applicationId,
                                               SubmissionCreateRequest request, String userId) {

        Application application = null;
        if (!ObjectUtils.isEmpty(applicationId)) {
            // Check if application exists
            application = applicationRepository.findById(applicationId)
                    .orElseThrow(() -> new ResourceNotFoundException("Application", "id", applicationId));
        }
        
        Submission submission = Submission.builder()
                .workspaceId(workspaceId)
                .videoUrl(request.getVideoUrl())
                .application(Optional.of(application).orElse(null))
                .videoThumbnailUrl(request.getVideoThumbnailUrl())
                .videoDuration(request.getVideoDuration())
                .videoResolution(request.getVideoResolution())
//                .processingStatus("PROCESSING") // Initial status
                .createdBy(userId)
                .build();

        submission = submissionRepository.save(submission);

        return SubmissionResponse.from(submission);
    }

    @Transactional(readOnly = true)
    public SubmissionResponse getSubmission(String submissionId) {
        Submission submission = findSubmissionById(submissionId);
        return SubmissionResponse.from(submission);
    }

    @Transactional
    public SubmissionResponse addComment(String submissionId, String userId, String content) {
        Submission submission = findSubmissionById(submissionId);

        SubmissionComment comment = SubmissionComment.builder()
                .submission(submission)
                .content(content)
                .createdBy(userId)
                .build();

        submission.getComments().add(comment);
        return SubmissionResponse.from(submissionRepository.save(submission));
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
        String workspaceId = WorkspaceContext.getWorkspaceId();

        return submissionRepository.findOne(
                (root, query, cb) -> cb.and(
                        cb.equal(root.get("id"), id),
                        cb.equal(root.get("workspaceId"), workspaceId)
                )
        ).orElseThrow(() -> new ResourceNotFoundException("Submission", "id", id));
    }
}