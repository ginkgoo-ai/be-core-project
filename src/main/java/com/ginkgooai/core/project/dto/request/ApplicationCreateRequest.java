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
    @Schema(description = "A existed talent for this application")
    private String talentId;

    @Schema(description = "A new talent for this application. " +
            "Must provide either talentId or talent, but not both.")
    public TalentRequest talent;
   
    @Schema(description = "List of video urls associated with this application")
    private List<String> videoUrls;
}