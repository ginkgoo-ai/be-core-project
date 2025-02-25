package com.ginkgooai.core.project.dto.response;

import com.ginkgooai.core.project.domain.ActivityStatus;
import com.ginkgooai.core.common.bean.ActivityType;
import com.ginkgooai.core.project.domain.ProjectActivity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Response payload for a project activity")
public class ProjectActivityResponse {
    @Schema(description = "ID of the activity", example = "activity123")
    private String id;

    @Schema(description = "Activity type", example = "ROLE_ADDED")
    private ActivityType activityType;

    @Schema(description = "Activity status", example = "SUBMITTED")
    private ActivityStatus status;

    @Schema(description = "Description of the activity", example = "Role added for Lead Character")
    private String description;

    @Schema(description = "Creation timestamp", example = "2025-02-21T10:00:00")
    private String createdAt;

    @Schema(description = "Project ID associated with the activity", example = "proj123")
    private String projectId;

    public static ProjectActivityResponse mapToProjectActivityResponse(ProjectActivity activity) {
        ProjectActivityResponse response = new ProjectActivityResponse();
        response.setId(activity.getId());
        response.setActivityType(activity.getActivityType());
        response.setStatus(activity.getStatus());
        response.setDescription(activity.getDescription());
        response.setCreatedAt(activity.getCreatedAt().toString());
        response.setProjectId(activity.getProject().getId());
        return response;
    }
}