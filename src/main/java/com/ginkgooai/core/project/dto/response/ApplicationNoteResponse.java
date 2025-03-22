package com.ginkgooai.core.project.dto.response;

import java.time.LocalDateTime;

import com.ginkgooai.core.project.client.identity.dto.UserInfoResponse;
import com.ginkgooai.core.project.domain.application.ApplicationNote;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

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

    @Schema(description = "Name of the user who created the note")
    private String userName;

    @Schema(description = "Profile picture URL of the user who created the note")
    private String userPicture;

    @Schema(description = "When the note was created")
    private LocalDateTime createdAt;

    @Schema(description = "When the note was last updated")
    private LocalDateTime updatedAt;

    public static ApplicationNoteResponse from(ApplicationNote note, UserInfoResponse user) {
        return ApplicationNoteResponse.builder()
                .id(note.getId())
                .content(note.getContent())
                .createdBy(note.getCreatedBy())
                .userPicture(user.getPicture())
                .userName(user.getName())
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .build();
    }
}