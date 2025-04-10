package com.ginkgooai.core.project.dto.response;

import com.ginkgooai.core.project.client.identity.dto.UserInfoResponse;
import com.ginkgooai.core.project.domain.talent.TalentComment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object containing talent comment information")
public class TalentCommentResponse {

    @Schema(description = "Unique identifier for the comment")
    private String id;

    @Schema(description = "Content of the comment")
    private String content;

    @Schema(description = "User ID who created the comment")
    private String createdBy;

    @Schema(description = "Display name of the user who created the comment")
    private String userName;

    @Schema(description = "URL to the user's profile picture")
    private String userPicture;

    @Schema(description = "Parent comment ID if this is a reply")
    private String parentId;

    @Schema(description = "Timestamp when the comment was created")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp of the last update to the comment")
    private LocalDateTime updatedAt;

    /**
     * Convert TalentComment entity to TalentCommentResponse
     */
    public static TalentCommentResponse from(TalentComment comment) {
        return TalentCommentResponse.builder()
            .id(comment.getId())
            .content(comment.getContent())
            .createdBy(comment.getCreatedBy())
            .parentId(comment.getParentId())
            .createdAt(comment.getCreatedAt())
            .updatedAt(comment.getUpdatedAt())
            .build();
    }

    /**
     * Convert TalentComment entity to TalentCommentResponse with user info
     */
    public static TalentCommentResponse from(TalentComment comment, UserInfoResponse userInfo) {
        TalentCommentResponse response = from(comment);
        if (userInfo != null) {
            response.setUserName(userInfo.getName());
            response.setUserPicture(userInfo.getPicture());
        }
        return response;
    }
}