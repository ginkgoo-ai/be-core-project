package com.ginkgooai.core.project.dto.response;

import com.ginkgooai.core.project.client.identity.dto.UserInfo;
import com.ginkgooai.core.project.domain.application.CommentType;
import com.ginkgooai.core.project.domain.application.Submission;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Builder
@Schema(description = "Submission response containing submission details and status")
public class SubmissionResponse {
    @Schema(description = "Unique identifier of the submission",
            example = "123e4567-e89b-12d3-a456-426614174000")
    private String id;

    @Schema(description = "Project identifier associated with this submission",
            example = "project-456")
    private String projectId;

    @Schema(description = "Application identifier associated with this submission",
            example = "application-456")
    private String applicationId;

    @Schema(description = "Role identifier for which this submission was made",
            example = "role-789")
    private String roleId;

    @Schema(description = "Name of the submitted video",
            example = "My Submission")
    private String videoName;

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

    @Schema(description = "Whether this submission is shortlisted by current user",
            example = "true")
    private Boolean shortlisted;

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

    /**
     * Converts a Submission into a SubmissionResponse DTO, enriching it with associated user details.
     *
     * <p>This method maps core fields from the Submission—including project, application, and video details,
     * timestamps, and creator information—into a corresponding DTO. It builds a lookup map from the provided
     * user list to annotate internal comment data with user-specific information. Public comments are included
     * only if the current user is the creator of the submission, and the method also determines whether the
     * submission is shortlisted by the current user.</p>
     *
     * @param submission the submission to convert
     * @param users the list of user information used to enhance comment details
     * @param userId the identifier of the current user for filtering public comments and determining shortlist status
     * @return a fully populated SubmissionResponse DTO reflecting the submission's details
     */
    public static SubmissionResponse from(Submission submission, List<UserInfo> users, String userId) {
        Map<String, UserInfo> userInfoMap = users.stream()
                .collect(Collectors.toMap(UserInfo::getId, user -> user));

        return SubmissionResponse.builder()
                .id(submission.getId())
                .projectId(submission.getApplication().getProject().getId())
                .applicationId(submission.getApplication().getId())
                .roleId(submission.getApplication().getRole().getId())
                .videoName(submission.getVideoName())
                .videoUrl(submission.getVideoUrl())
                .videoThumbnailUrl(submission.getVideoThumbnailUrl())
                .videoDuration(submission.getVideoDuration())
                .videoResolution(submission.getVideoResolution())
                .createdBy(submission.getCreatedBy())
                .createdAt(submission.getCreatedAt())
                .updatedAt(submission.getUpdatedAt())
                .internalComments(submission.getComments().stream()
                        .filter(comment -> CommentType.INTERNAL.equals(comment.getType()))
                        .map(t -> SubmissionCommentResponse.from(t, userInfoMap.get(t.getCreatedBy())))
                        .toList())
                .publicComments(userId.equals(submission.getCreatedBy()) ? submission.getComments().stream()
                        .filter(comment -> CommentType.PUBLIC.equals(comment.getType()))
                        .map(t -> SubmissionCommentResponse.from(t, userInfoMap.get(t.getCreatedBy())))
                        .toList() : null)
                .shortlisted(submission.getShortlistItems() != null &&
                        submission.getShortlistItems().stream()
                                .anyMatch(item -> item.getShortlist().getOwnerId().equals(userId)))
                .build();
    }

    /**
     * Creates a SubmissionResponse from a Submission using an empty list for user details.
     *
     * <p>This overload delegates to {@link SubmissionResponse#from(Submission, List, String)}
     * when no user information is provided.
     *
     * @param submission the Submission object to convert
     * @param userId the identifier of the current user, used for filtering comment visibility
     * @return a SubmissionResponse representing the converted submission data
     */
    public static SubmissionResponse from(Submission submission, String userId) {
        return SubmissionResponse.from(submission, Collections.EMPTY_LIST, userId); 
    }

}

        