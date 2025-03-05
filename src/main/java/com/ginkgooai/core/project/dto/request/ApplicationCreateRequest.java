package com.ginkgooai.core.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Request object for creating a new application")
public class ApplicationCreateRequest {
    
    @Schema(description = "Project ID this application belongs to", required = true)
    @NotBlank(message = "Project ID is required")
    private String projectId;

    @Schema(description = "Role ID the talent is applying for", required = true)
    @NotBlank(message = "Role ID is required")
    private String roleId;

    // Talent Information
    @Schema(description = "Identity of the talent", required = true)
    @NotBlank(message = "Talent ID is required")
    private String talentId;
   
    @Schema(description = "List of video submission IDs associated with this application")
    private List<String> submissionIds;
}