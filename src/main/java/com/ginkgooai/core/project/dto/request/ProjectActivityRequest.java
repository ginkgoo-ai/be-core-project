package com.ginkgooai.core.project.dto.request;

import com.ginkgooai.core.project.domain.ActivityStatus;
import com.ginkgooai.core.common.bean.ActivityType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request payload for logging a project activity")
public class ProjectActivityRequest {
    @Schema(description = "Activity type", example = "ROLE_ADDED")
    private ActivityType activityType;

    @Schema(description = "Activity status", example = "SUBMITTED")
    private ActivityStatus status;

    @Schema(description = "Description of the activity", example = "Role added for Lead Character")
    private String description;
}