package com.ginkgooai.core.project.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectRoleStatisticsResponse {
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

    @Schema(description = "Total number of talents for this role")
    private long total;

    @Schema(description = "Number of talents added")
    private long added;

    @Schema(description = "Number of talents who submitted")
    private long submitted;

    @Schema(description = "Number of talents shortlisted")
    private long shortlisted;

    @Schema(description = "Number of talents declined")
    private long declined;
}