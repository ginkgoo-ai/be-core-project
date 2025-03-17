package com.ginkgooai.core.project.dto.request;

import com.ginkgooai.core.project.domain.project.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request payload for creating or updating a project")
public class ProjectUpdateRequest {

    @Schema(description = "Name of the project", example = "Summer Feature 2025")
    private String name;

    @Schema(description = "Description of the project", example = "A summer feature film about...")
    private String description;

    @Schema(description = "Plot line of the project", example = "A young hero embarks on a journey...")
    private String plotLine;

    @Schema(description = "Status of the project", example = "IN_PROGRESS")
    private ProjectStatus status;

    @Schema(description = "Producer of the project", example = "Mark Ronson")
    private String producer;
}