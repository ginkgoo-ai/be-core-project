package com.ginkgooai.core.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Request object for guest creating a public comment")
public class GuestCommentCreateRequest {
    
    @NotBlank(message = "Comment content cannot be empty")
    @Schema(description = "Content of the comment", example = "Great performance!")
    private String content;
    
    @Schema(description = "ID of the parent comment for replies", example = "comment_123")
    private String parentCommentId;
}