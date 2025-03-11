package com.ginkgooai.core.project.dto.response;

import com.ginkgooai.core.project.domain.role.ProjectRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Response payload for a project role")
public class ProjectRoleResponse {
    @Schema(description = "ID of the role", example = "role123")
    private String id;

    @Schema(description = "Name of the role", example = "Lead Character")
    private String name;

    @Schema(description = "Sides for the role", example = "['side1', 'side2']")
    private String[] sides;

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

    @Schema(description = "Project ID associated with the role", example = "proj123")
    private String projectId;

    // Mapping methods (simplified, can use ModelMapper or MapStruct in practice)
    public static ProjectRoleResponse from(ProjectRole role) {
        ProjectRoleResponse response = new ProjectRoleResponse();
        response.setId(role.getId());
        response.setName(role.getName());
        response.setSides(role.getSides());
        response.setCharacterDescription(role.getCharacterDescription());
        response.setSelfTapeInstructions(role.getSelfTapeInstructions());
        response.setIsActive(role.getIsActive());
        response.setProjectId(role.getProject().getId());
        return response;
    }
}