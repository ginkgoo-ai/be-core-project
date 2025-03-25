package com.ginkgooai.core.project.dto.request;

import java.util.List;

import com.ginkgooai.core.project.domain.project.ProjectStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request payload for creating or updating a project")
public class ProjectCreateRequest {

    @Schema(description = "Name of the project", example = "Summer Feature 2025")
    private String name;

    @Schema(description = "Description of the project", example = "A summer feature film about...")
    private String description;

    @Schema(description = "Plot line of the project", example = "A young hero embarks on a journey...")
    private String plotLine;

    @Schema(description = "Status of the project", example = "IN_PROGRESS")
    private ProjectStatus status;

    @Schema(description = "List of roles associated with the project")
    private List<ProjectRoleRequest> roles;

    @Schema(description = "Producer of the project", example = "Mark Ronson")
    private String producer;

    @Schema(description = "Project poster Url", example = "www.example.com/poster.jpg")
    private String posterUrl;

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getPlotLine() {
        return plotLine;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public List<ProjectRoleRequest> getRoles() {
        return roles;
    }

    public String getProducer() {
        return producer;
    }

    public String getPosterUrl() {
        return posterUrl;
    }
}