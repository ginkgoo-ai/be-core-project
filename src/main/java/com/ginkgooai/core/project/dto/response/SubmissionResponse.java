package com.ginkgooai.core.project.dto.response;

import com.ginkgooai.core.common.constant.ContextsConstant;
import com.ginkgooai.core.common.utils.ContextUtils;
import com.ginkgooai.core.project.client.identity.dto.UserInfoResponse;
import com.ginkgooai.core.project.domain.application.CommentType;
import com.ginkgooai.core.project.domain.application.Submission;
import com.ginkgooai.core.project.domain.application.SubmissionComment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Data
@Builder
@Schema(description = "Submission response containing submission details and status")
public class SubmissionResponse {
    @Schema(description = "Unique identifier of the submission", example = "123e4567-e89b-12d3-a456-426614174000")
    private String id;

    @Schema(description = "Project identifier associated with this submission", example = "project-456")
    private String projectId;

    @Schema(description = "Application identifier associated with this submission", example = "application-456")
    private String applicationId;

    @Schema(description = "Talent information related to this submission")
    private TalentBasicResponse talent;

    @Schema(description = "Role identifier for which this submission was made", example = "role-789")
    private String roleId;

    @Schema(description = "Name of the submitted video", example = "My Submission")
    private String videoName;

    // Video related fields
    @Schema(description = "URL of the submitted video", example = "https://storage.example.com/videos/submission-123.mp4")
    private String videoUrl;

    @Schema(description = "URL of the video thumbnail", example = "https://storage.example.com/thumbnails/submission-123.jpg")
    private String videoThumbnailUrl;

    @Schema(description = "Duration of the video in seconds", example = "180")
    private Long videoDuration;

    @Schema(description = "Resolution of the video", example = "1920x1080")
    private String videoResolution;

    @Schema(description = "Number of times the video has been viewed", example = "42")
    private Long viewCount;

    @Schema(description = "Whether this submission is shortlisted by current user", example = "true")
    private Boolean shortlisted;

    // Metadata
    @Schema(description = "User ID of the submission creator", example = "user-123")
    private String createdBy;

    @Schema(description = "Timestamp when the submission was created", example = "2025-03-03T02:09:57.713Z")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when the submission was last updated", example = "2025-03-03T02:09:57.713Z")
    private LocalDateTime updatedAt;

    @Schema(description = "List of internal comments associated with this submission")
    private List<SubmissionCommentResponse> internalComments;

    @Schema(description = "List of public comments associated with this submission")
    private List<SubmissionCommentResponse> publicComments;

	@Schema(description = "Number of public comments associated with this submission")
	private long publicCommentCount;

	@Schema(description = "Number of internal comments associated with this submission")
	private long internalCommentCount;

    public static SubmissionResponse from(Submission submission, List<UserInfoResponse> users, String userId) {
        Map<String, UserInfoResponse> userInfoMap = CollectionUtils.emptyIfNull(users).stream()
            .collect(Collectors.toMap(UserInfoResponse::getId, user -> user));

        List<String> role = ContextUtils.get().get(ContextsConstant.USER_ROLE, List.class);
        boolean isTalentRole = role.size() == 1 && role.get(0).equals("ROLE_TALENT");

        return SubmissionResponse.builder()
            .id(submission.getId())
            .projectId(submission.getApplication().getProject().getId())
            .applicationId(submission.getApplication().getId())
            .talent(TalentBasicResponse.from(submission.getApplication().getTalent()))
            .roleId(submission.getApplication().getRole().getId())
            .videoName(submission.getVideoName())
            .videoUrl(submission.getVideoUrl())
            .videoThumbnailUrl(submission.getVideoThumbnailUrl())
            .videoDuration(submission.getVideoDuration())
            .videoResolution(submission.getVideoResolution())
            .viewCount(submission.getViewCount())
            .createdBy(submission.getCreatedBy())
            .createdAt(submission.getCreatedAt())
            .updatedAt(submission.getUpdatedAt())
            .publicComments(submission.getComments().stream()
                .filter(comment -> CommentType.PUBLIC.equals(comment.getType()))
                .filter(comment -> isTalentRole ? comment.getCreatedBy()
                    .equals(userId) : true)
                .map(t -> SubmissionCommentResponse.from(t,
                    userInfoMap.get(t.getCreatedBy())))
                .toList())
            .internalComments(userId.equals(submission.getCreatedBy()) ? submission.getComments()
                .stream()
                .filter(comment -> CommentType.INTERNAL.equals(comment.getType()))
                .map(t -> SubmissionCommentResponse.from(t,
                    userInfoMap.get(t.getCreatedBy())))
                .toList() : null)
            .shortlisted(submission.getShortlistItems() != null &&
                submission.getShortlistItems().stream()
                    .anyMatch(item -> item.getShortlist().getCreatedBy()
                        .equals(userId)))
            .build();
    }

    public static SubmissionResponse from(Submission submission, String userId) {
		List<SubmissionComment> comments = Optional.ofNullable(submission.getComments()).orElse(List.of());
        return SubmissionResponse.builder()
            .id(submission.getId())
            .projectId(submission.getApplication().getProject().getId())
            .applicationId(submission.getApplication().getId())
            .talent(TalentBasicResponse.from(submission.getApplication().getTalent()))
            .roleId(submission.getApplication().getRole().getId())
            .videoName(submission.getVideoName())
            .videoUrl(submission.getVideoUrl())
            .videoThumbnailUrl(submission.getVideoThumbnailUrl())
            .videoDuration(submission.getVideoDuration())
            .videoResolution(submission.getVideoResolution())
            .viewCount(submission.getViewCount())
			.publicCommentCount(comments.stream().filter(t -> CommentType.PUBLIC.equals(t.getType())).count())
			.internalCommentCount(comments.stream().filter(t -> CommentType.INTERNAL.equals(t.getType())).count())
            .createdBy(submission.getCreatedBy())
            .createdAt(submission.getCreatedAt())
            .updatedAt(submission.getUpdatedAt())
            .shortlisted(submission.getShortlistItems() != null &&
                submission.getShortlistItems().stream()
                    .anyMatch(item -> item.getShortlist().getOwnerId()
                        .equals(userId)))
            .build();
    }

}
