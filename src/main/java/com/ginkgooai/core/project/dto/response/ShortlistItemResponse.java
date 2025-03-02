package com.ginkgooai.core.project.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Response object containing shortlist item details")
public class ShortlistItemResponse {
    
    @Schema(description = "Unique identifier of the shortlist item", 
            example = "sli_12345")
    private String id;
    
    @Schema(description = "Video submission details")
    private VideoSubmissionResponse video;
    
    @Schema(description = "Notes about the video", 
            example = "Great performance, particularly strong in emotional scenes")
    private String notes;
    
    @Schema(description = "Order in the shortlist", 
            example = "1")
    private Integer order;
    
    @Schema(description = "User who added the video", 
            example = "user_12345")
    private String addedBy;
    
    @Schema(description = "Timestamp when the video was added")
    private LocalDateTime addedAt;
}