package com.ginkgooai.core.project.dto.response;

import com.ginkgooai.core.project.domain.application.CommentType;
import com.ginkgooai.core.project.domain.application.Submission;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@Schema(description = "Submission response containing submission details, video information, and comments")
public class SubmissionResponse {
    @Schema(description = "Unique identifier of the submission",
            example = "123e4567-e89b-12d3-a456-426614174000")
    private String id;

    @Schema(description = "Project identifier associated with this submission",
            example = "project-456")
    private String projectId;

    @Schema(description = "Role identifier for which this submission was made",
            example = "role-789")
    private String roleId;
    
    // Video related fields
    @Schema(description = "URL of the submitted video",
            example = "https://storage.example.com/videos/submission-123.mp4")
    private String videoUrl;

    @Schema(description = "URL of the video thumbnail",
            example = "https://storage.example.com/thumbnails/submission-123.jpg")
    private String videoThumbnailUrl;

    @Schema(description = "Duration of the video in seconds",
            example = "180")
    private Long videoDuration;

    @Schema(description = "Resolution of the video",
            example = "1920x1080")
    private String videoResolution;

    @Schema(description = "Current status of the video processing",
            example = "PROCESSED",
            allowableValues = {"PROCESSING", "PROCESSED", "FAILED"})
    private String videoStatus;
    
    // Metadata
    @Schema(description = "User ID of the submission creator",
            example = "user-123")
    private String createdBy;

    @Schema(description = "Timestamp when the submission was created",
            example = "2025-03-03T02:09:57.713Z")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when the submission was last updated",
            example = "2025-03-03T02:09:57.713Z")
    private LocalDateTime updatedAt;
    
    @Schema(description = "List of internal comments associated with this submission")
    private List<SubmissionCommentResponse> internalComments;

    @Schema(description = "List of public comments associated with this submission")
    private List<SubmissionCommentResponse> publicComments;

    @Schema(description = "Converts a Submission entity to SubmissionResponse DTO")
    public static SubmissionResponse from(Submission submission, String userId) {
        return SubmissionResponse.builder()
                .id(submission.getId())
                .projectId(submission.getApplication().getProject().getId())
                .roleId(submission.getApplication().getRole().getId())
                .videoUrl(submission.getVideoUrl())
                .videoThumbnailUrl(submission.getVideoThumbnailUrl())
                .videoDuration(submission.getVideoDuration())
                .videoResolution(submission.getVideoResolution())
                .createdBy(submission.getCreatedBy())
                .createdAt(submission.getCreatedAt())
                .updatedAt(submission.getUpdatedAt())
                .internalComments(submission.getComments().stream()
                        .filter(comment -> CommentType.INTERNAL.equals(comment.getType()))
                        .map(SubmissionCommentResponse::from)
                        .toList())
                .publicComments(userId.equals(submission.getCreatedBy()) ? submission.getComments().stream()
                        .filter(comment -> CommentType.PUBLIC.equals(comment.getType()))
                        .map(SubmissionCommentResponse::from)
                        .toList() : null)
                .build();
    }
}