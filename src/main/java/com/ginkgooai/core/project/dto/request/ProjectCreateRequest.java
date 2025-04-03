package com.ginkgooai.core.project.dto.request;

import com.ginkgooai.core.project.domain.project.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Request payload for creating or updating a project")
public class ProjectCreateRequest {

    @Schema(description = "Name of the project", example = "Summer Feature 2025")
    @NotEmpty
    private String name;

    @Schema(description = "Description of the project", example = "A summer feature film about...")
    @NotEmpty
    private String description;

    @Schema(description = "Plot line of the project", example = "A young hero embarks on a journey...")
    @NotEmpty
    private String plotLine;

    @Schema(description = "Status of the project", example = "IN_PROGRESS")
    @NotNull
    private ProjectStatus status;

    @Schema(description = "List of roles associated with the project")
    private List<ProjectRoleRequest> roles;

    @Schema(description = "Producer of the project", example = "Mark Ronson")
    @NotEmpty
    private String producer;

    @Schema(description = "Project poster Url", example = "www.example.com/poster.jpg")
    @NotEmpty
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