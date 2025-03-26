package com.ginkgooai.core.project.dto.response;

import com.ginkgooai.core.project.client.identity.dto.UserInfoResponse;
import com.ginkgooai.core.project.domain.application.SubmissionComment;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Optional;

@Data
@Builder
@Schema(description = "Response object containing submission comment details")
public class SubmissionCommentResponse {
    
    @Schema(description = "Unique identifier of the comment",
            example = "123e4567-e89b-12d3-a456-426614174000")
    private String id;

    @Schema(description = "Content of the comment",
            example = "Great performance in this submission!")
    private String content;

    @Schema(description = "User ID of the comment creator",
            example = "user-123")
    private String createdBy;

    @Schema(description = "User's name")
    private String userName;

    @Schema(description = "User's avatar URL")
    private String userPicture;

    @Schema(description = "Timestamp when the comment was created",
            example = "2025-03-03T02:09:57.713Z")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when the comment was last updated",
            example = "2025-03-03T02:09:57.713Z")
    private LocalDateTime updatedAt;

    //For internal user comment
    public static SubmissionCommentResponse from(SubmissionComment comment, @Nullable UserInfoResponse userInfo) {
        return SubmissionCommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .createdBy(comment.getCreatedBy())
                .userName(Optional.ofNullable(userInfo).map(UserInfoResponse::getName).orElse(comment.getCreatedBy()))
                .userPicture(Optional.ofNullable(userInfo).map(UserInfoResponse::getPicture).orElse(null))
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }

    //For public user comment
    public static SubmissionCommentResponse from(SubmissionComment comment) {
        return SubmissionCommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .createdBy(comment.getCreatedBy())
                .userName(comment.getCreatedBy())
                .userPicture("") //TODO: Add system default guest picture
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}