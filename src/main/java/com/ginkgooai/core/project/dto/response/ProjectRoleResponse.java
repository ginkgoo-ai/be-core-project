package com.ginkgooai.core.project.dto.response;

import com.ginkgooai.core.project.client.storage.dto.CloudFileResponse;
import com.ginkgooai.core.project.domain.role.ProjectRole;
import com.ginkgooai.core.project.domain.role.RoleStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response payload for a project role")
public class ProjectRoleResponse {
    @Schema(description = "ID of the role", example = "role123")
    private String id;

    @Schema(description = "Name of the role", example = "Lead Character")
    private String name;

    @Schema(description = "Sides for the role", example = "['side1', 'side2']")
    private List<CloudFileResponse> sides;

    @Schema(description = "Character description", example = "A brave young hero")
    private String characterDescription;

    @Schema(description = "Self-tape instructions", example = "Prepare a 2-minute monologue")
    private String selfTapeInstructions;

    @Schema(description = "Audition notes", example = "Focus on emotional range")
    private String auditionNotes;

    @Schema(description = "Age range for the role", example = "25-30")
    private String ageRange;

    @Schema(description = "Gender for the role", example = "Any")
    private String gender;

    @Schema(description = "Whether the role is active", example = "true")
    private Boolean isActive;

	@Schema(description = "Role status", example = "CASTING")
	private RoleStatus status;

    @Schema(description = "Project ID associated with the role", example = "proj123")
    private String projectId;

    public static ProjectRoleResponse from(ProjectRole role) {
        if (role == null) {
            return null;
        }

        return ProjectRoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .characterDescription(role.getCharacterDescription())
                .selfTapeInstructions(role.getSelfTapeInstructions())
                .isActive(role.getIsActive())
                .projectId(role.getProject() != null ? role.getProject().getId() : null)
			.status(role.getStatus())
                .build();
    }

    public static ProjectRoleResponse from(ProjectRole role, Map<String, CloudFileResponse> roleSideFilesMap) {
        ProjectRoleResponse response = from(role);
        if (response == null) {
            return null;
        }

        List<CloudFileResponse> sideFiles = role.getSides() != null && roleSideFilesMap != null
                ? Arrays.stream(role.getSides())
                        .filter(sideId -> sideId != null && roleSideFilesMap.containsKey(sideId))
                        .map(roleSideFilesMap::get)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList())
                : new ArrayList<>();

        response.setSides(sideFiles);
        return response;
    }
}