package com.ginkgooai.core.project.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "Project statistics overview")
@Data
public class ProjectStatisticsResponse {

    @Schema(description = "Number of active projects", example = "30")
    private int activeProjects;

    @Schema(description = "Number of roles yet to be filled", example = "77")
    private int rolesToFill;

    @Schema(description = "Number of submissions pending review", example = "137")
    private int pendingSubmissions;

    @Schema(description = "Number of submissions with unviewed videos", example = "8")
    private int unviewedVideos;

    public ProjectStatisticsResponse(int activeProjects, int rolesToFill, int pendingSubmissions, int unviewedVideos) {
        this.activeProjects = activeProjects;
        this.rolesToFill = rolesToFill;
        this.pendingSubmissions = pendingSubmissions;
        this.unviewedVideos = unviewedVideos;
    }

}
