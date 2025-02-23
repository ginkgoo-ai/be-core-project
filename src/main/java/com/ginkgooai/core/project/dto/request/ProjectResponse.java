package com.ginkgooai.core.project.dto.request;

import com.ginkgooai.core.project.domain.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Schema(description = "Response payload for a project")
public class ProjectResponse {

    @Schema(description = "ID of the project", example = "proj123")
    private String id;

    @Schema(description = "Name of the project", example = "Summer Feature 2025")
    private String name;

    @Schema(description = "Description of the project", example = "A summer feature film about...")
    private String description;

    @Schema(description = "Plot line of the project", example = "A young hero embarks on a journey...")
    private String plotLine;

    @Schema(description = "Status of the project", example = "DRAFTING, ACTIVE, COMPLETED, PENDING_REVIEW")
    private ProjectStatus status;

    @Schema(description = "Owner ID of the project", example = "user123")
    private String ownerId;

    @Schema(description = "Last activity timestamp", example = "2025-02-20T10:00:00")
    private LocalDateTime lastActivityAt;

    @Schema(description = "Timestamp when the project was created", example = "2025-02-20T09:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when the project was last updated", example = "2025-02-20T10:30:00")
    private LocalDateTime updatedAt;

    @Schema(description = "List of role IDs associated with the project")
    private Set<String> roleIds;

    @Schema(description = "List of NDA IDs associated with the project")
    private Set<String> ndaIds;

    @Schema(description = "List of member IDs associated with the project")
    private Set<String> memberIds;

    @Schema(description = "List of activity IDs associated with the project")
    private Set<String> activityIds;
}