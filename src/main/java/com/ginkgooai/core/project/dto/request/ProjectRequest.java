package com.ginkgooai.core.project.dto.request;

import com.ginkgooai.core.project.domain.project.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@Schema(description = "Request payload for creating or updating a project")
public class ProjectRequest {

    @Schema(description = "Name of the project", example = "Summer Feature 2025")
    private String name;

    @Schema(description = "Description of the project", example = "A summer feature film about...")
    private String description;

    @Schema(description = "Plot line of the project", example = "A young hero embarks on a journey...")
    private String plotLine;

    @Schema(description = "Status of the project", example = "In Progress")
    private ProjectStatus status;

    @Schema(description = "Owner ID of the project", example = "user123")
    private String ownerId;

    @Schema(description = "List of roles associated with the project")
    private List<ProjectRoleRequest> roles;
    
    @Schema(description = "Producer of the project", example = "Mark Ronson")
    private String producer;

    @Schema(description = "List of NDA IDs associated with the project")
    private Set<String> ndaIds;

    @Schema(description = "List of member IDs associated with the project")
    private Set<String> memberIds;

    @Schema(description = "List of activity IDs associated with the project")
    private Set<String> activityIds;
}