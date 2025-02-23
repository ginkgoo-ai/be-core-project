package com.ginkgooai.core.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request payload for creating or updating a project role")
public class ProjectRoleRequest {
    @Schema(description = "Name of the role", example = "Lead Character")
    private String name;

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
}