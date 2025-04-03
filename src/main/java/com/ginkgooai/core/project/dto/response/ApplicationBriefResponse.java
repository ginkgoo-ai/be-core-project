package com.ginkgooai.core.project.dto.response;

import com.ginkgooai.core.project.domain.application.ApplicationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Brief information about talent's application")
public class ApplicationBriefResponse {
    @Schema(description = "Application ID", example = "app_123")
    private String id;

    @Schema(description = "Project ID", example = "proj_456")
    private String projectId;

    @Schema(description = "Project name", example = "Marvel Movie")
    private String projectName;

    @Schema(description = "Role name", example = "Supporting Actor")
    private String roleName;

    @Schema(description = "Application status", example = "SUBMITTED")
    private ApplicationStatus status;

    @Schema(description = "Application submission time")
    private LocalDateTime submittedAt;
}