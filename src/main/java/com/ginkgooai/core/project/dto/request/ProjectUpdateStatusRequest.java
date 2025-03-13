package com.ginkgooai.core.project.dto.request;

import com.ginkgooai.core.project.domain.project.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request payload for update project status")
public class ProjectUpdateStatusRequest {

    @Schema(description = "Status of the project", example = "IN_PROGRESS")
    private ProjectStatus status;
}