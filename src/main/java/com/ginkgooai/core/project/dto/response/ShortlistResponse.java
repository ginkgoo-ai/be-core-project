package com.ginkgooai.core.project.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Schema(description = "Response object containing shortlist details")
public class ShortlistResponse {
    
    @Schema(description = "Unique identifier of the shortlist", 
            example = "sl_12345")
    private String id;
    
    @Schema(description = "Workspace identifier", 
            example = "ws_12345")
    private String workspaceId;
    
    @Schema(description = "Name of the shortlist", 
            example = "Round 1 Selected Videos")
    private String name;
    
    @Schema(description = "Description of the shortlist", 
            example = "Selected videos from first round of auditions")
    private String description;
    
    @Schema(description = "Project identifier", 
            example = "proj_12345")
    private String projectId;
    
    @Schema(description = "Role identifier", 
            example = "role_12345")
    private String roleId;
    
    @Schema(description = "List of videos in the shortlist")
    private List<ShortlistItemResponse> items;
    
    @Schema(description = "User who created the shortlist", 
            example = "user_12345")
    private String createdBy;
    
    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;
    
    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}