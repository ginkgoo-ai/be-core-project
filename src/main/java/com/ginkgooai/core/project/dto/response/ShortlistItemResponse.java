package com.ginkgooai.core.project.dto.response;

import com.ginkgooai.core.project.domain.application.ShortlistItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Schema(description = "Response object containing shortlist item details")
public class ShortlistItemResponse {
    
    @Schema(description = "Unique identifier of the shortlist item", 
            example = "sli_12345")
    private String id;

    @Schema(description = "Application identifier associated with this submission",
            example = "application-456")
    private String applicationId;
    
    @Schema(description = "Submission lists")
    private List<SubmissionResponse> submissions;
    
    @Schema(description = "Order in the shortlist", 
            example = "1")
    private Integer order;
    
    @Schema(description = "User who added the video", 
            example = "user_12345")
    private String createdBy;
    
    @Schema(description = "Timestamp when the video was added")
    private LocalDateTime createdAt;
    
    public static ShortlistItemResponse from(ShortlistItem shortlistItem, String userId) {
        return ShortlistItemResponse.builder()
                .id(shortlistItem.getId())
                .applicationId(shortlistItem.getApplication().getId())
                .submissions(shortlistItem.getSubmissions().stream().map(t -> SubmissionResponse.from(t, userId)).toList())
                .order(shortlistItem.getSortOrder())
                .createdBy(shortlistItem.getCreatedBy())
                .createdAt(shortlistItem.getCreatedAt())
                .build();
    }
}