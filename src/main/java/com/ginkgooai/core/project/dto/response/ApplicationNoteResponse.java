package com.ginkgooai.core.project.dto.response;

import com.ginkgooai.core.project.domain.application.ApplicationNote;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Response object for application note")
public class ApplicationNoteResponse {
    
    @Schema(description = "Unique identifier of the note")
    private String id;

    @Schema(description = "Content of the note")
    private String content;

    @Schema(description = "User ID who created the note")
    private String createdBy;

    @Schema(description = "When the note was created")
    private LocalDateTime createdAt;

    @Schema(description = "When the note was last updated")
    private LocalDateTime updatedAt;

    public static ApplicationNoteResponse from(ApplicationNote note) {
        return ApplicationNoteResponse.builder()
                .id(note.getId())
                .content(note.getContent())
                .createdBy(note.getCreatedBy())
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .build();
    }
}