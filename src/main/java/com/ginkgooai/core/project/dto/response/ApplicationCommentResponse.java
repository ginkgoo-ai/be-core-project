package com.ginkgooai.core.project.dto.response;

import com.ginkgooai.core.project.domain.application.ApplicationComment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Response object for application comment")
public class ApplicationCommentResponse {
    
    @Schema(description = "Unique identifier of the comment")
    private String id;

    @Schema(description = "Content of the comment")
    private String content;

    @Schema(description = "User ID who created the comment")
    private String createdBy;

    @Schema(description = "User's name")
    private String userName;

    @Schema(description = "User's avatar URL")
    private String userPicture;

    @Schema(description = "When the comment was created")
    private LocalDateTime createdAt;

    @Schema(description = "When the comment was last updated")
    private LocalDateTime updatedAt;

    public static ApplicationCommentResponse from(ApplicationComment comment) {
        return ApplicationCommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .createdBy(comment.getCreatedBy())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}