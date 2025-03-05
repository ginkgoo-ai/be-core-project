package com.ginkgooai.core.project.dto.request;

import com.ginkgooai.core.project.domain.application.CommentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Request object for creating a new comment")
public class CommentCreateRequest {
    
    @NotBlank(message = "Comment content cannot be empty")
    @Schema(description = "Content of the comment", example = "Great performance!")
    private String content;
    
    @NotNull(message = "Comment type must be specified")
    @Schema(description = "Type of visibility of the comment (PUBLIC or INTERNAL)", example = "PUBLIC")
    private CommentType type;
    
    @Schema(description = "ID of the parent comment for replies", example = "comment_123")
    private String parentCommentId;
}