package com.ginkgooai.core.project.dto.request;

import com.ginkgooai.core.project.client.storage.dto.CloudFileResponse;
import com.ginkgooai.core.project.domain.project.Project;
import com.ginkgooai.core.project.domain.project.ProjectStatus;
import com.ginkgooai.core.project.dto.response.ProjectRoleResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    @Schema(description = "Status of the project", example = "IN_PROGRESS")
    private ProjectStatus status;

    @Schema(description = "Owner ID of the project", example = "user123")
    private String createdBy;

    @Schema(description = "Last activity timestamp", example = "25/03/2025 10:00:00")
    private LocalDateTime lastActivityAt;

    @Schema(description = "Timestamp when the project was created", example = "25/03/2025 09:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when the project was last updated", example = "25/03/2025 10:30:00")
    private LocalDateTime updatedAt;

    @Schema(description = "List of roles associated with the project")
    private List<ProjectRoleResponse> roles;

    @Schema(description = "Number of roles in the project", example = "5")
    private Integer rolesCount;

    @Schema(description = "Workspace Id of this project", example = "work123")
    private String workspaceId;

    @Schema(description = "Project poster Url", example = "www.example.com/poster.jpg")
    private String posterUrl;

    @Schema(description = "Producer of the project", example = "Mark Ronson")
    private String producer;

    public static ProjectResponse from(Project project) {
        if (ObjectUtils.isEmpty(project.getRoles())) {
            project.setRoles(Collections.emptySet());
        }

        ProjectResponse response = new ProjectResponse();
        response.setId(project.getId());
        response.setName(project.getName());
        response.setDescription(project.getDescription());
        response.setPlotLine(project.getPlotLine());
        response.setStatus(project.getStatus());
        response.setCreatedBy(project.getCreatedBy());
        response.setCreatedAt(project.getCreatedAt());
        response.setUpdatedAt(project.getUpdatedAt());
        response.setRoles(project.getRoles().stream().map(t -> ProjectRoleResponse.from(t)).toList());
        response.setRolesCount(project.getRoles().size());
        response.setWorkspaceId(project.getWorkspaceId());
        response.setPosterUrl(project.getPosterUrl());
        response.setProducer(project.getProducer());
        return response;
    }

    public static ProjectResponse from(Project project, List<CloudFileResponse> roleSideFiles) {
        Map<String, CloudFileResponse> roleSideFilesMap = roleSideFiles.stream()
                .collect(Collectors.toMap(
                        file -> file.getId(),
                        Function.identity(),
                        (o, n) -> n));

        if (ObjectUtils.isEmpty(project.getRoles())) {
            project.setRoles(Collections.emptySet());
        }
        ProjectResponse response = new ProjectResponse();
        response.setId(project.getId());
        response.setName(project.getName());
        response.setDescription(project.getDescription());
        response.setPlotLine(project.getPlotLine());
        response.setStatus(project.getStatus());
        response.setCreatedBy(project.getCreatedBy());
        response.setCreatedAt(project.getCreatedAt());
        response.setUpdatedAt(project.getUpdatedAt());
        response.setRoles(project.getRoles().stream().map(t -> ProjectRoleResponse.from(t, roleSideFilesMap)).toList());
        response.setRolesCount(project.getRoles().size());
        response.setWorkspaceId(project.getWorkspaceId());
        response.setPosterUrl(project.getPosterUrl());
        response.setProducer(project.getProducer());
        return response;
    }

}