package com.ginkgooai.core.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Request object for creating a new note")
public class NoteCreateRequest {
    
    @NotBlank(message = "Note content cannot be empty")
    @Schema(description = "Content of the comment", example = "Great performance!")
    private String content;
}