package com.ginkgooai.core.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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
    @Schema(description = "List of existing talent IDs for this application. Must provide at least one talent ID.",
        required = true)
    @NotEmpty(message = "At least one Talent ID must be provided")
    private List<String> talentIds;

    @Schema(description = "List of video file Ids associated with this application")
    private List<String> videoIds;
}
