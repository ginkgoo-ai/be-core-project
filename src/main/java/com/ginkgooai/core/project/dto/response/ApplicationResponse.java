package com.ginkgooai.core.project.dto.response;

import com.ginkgooai.core.project.domain.application.Application;
import com.ginkgooai.core.project.domain.application.ApplicationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Schema(description = "Response object containing application details")
public class ApplicationResponse {
    
    @Schema(description = "Unique identifier of the application", example = "app_12345")
    private String id;

    @Schema(description = "Workspace identifier", example = "ws_12345")
    private String workspaceId;

    @Schema(description = "Project identifier", example = "proj_12345")
    private String projectId;

    @Schema(description = "Role identifier", example = "role_12345")
    private String roleId;

    @Schema(description = "Associated talent details")
    private TalentResponse talent;

//    @Schema(description = "List of video submissions")
//    private List<VideoSubmissionResponse> videos;

    @Schema(description = "Agency name", example = "Creative Artists Agency")
    private String agencyName;

    @Schema(description = "Agent name", example = "John Smith")
    private String agentName;

    @Schema(description = "Agent email", example = "john.smith@agency.com")
    private String agentEmail;

    @Schema(description = "Current application status", example = "PENDING")
    private ApplicationStatus status;

    @Schema(description = "Reviewer's identifier", example = "user_12345")
    private String reviewedBy;

    @Schema(description = "Review timestamp")
    private LocalDateTime reviewedAt;

    @Schema(description = "Review notes", example = "Great performance, perfect fit for the role")
    private String reviewNotes;

    @Schema(description = "Whether the application is shortlisted", example = "true")
    private boolean shortlisted;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
    
    public static ApplicationResponse from(Application application) {
        return ApplicationResponse.builder()
                .id(application.getId())
                .workspaceId(application.getWorkspaceId())
                .projectId(application.getProject().getId())
                .roleId(application.getRole().getId())
                .talent(TalentResponse.from(application.getTalent()))
                .agencyName(application.getAgencyName())
                .agentName(application.getAgentName())
                .agentEmail(application.getAgentEmail())
                .status(application.getStatus())
                .reviewedBy(application.getReviewedBy())
                .reviewedAt(application.getReviewedAt())
                .reviewNotes(application.getReviewNotes())
                .shortlisted(application.isShortlisted())
                .createdAt(application.getCreatedAt())
                .updatedAt(application.getUpdatedAt())
                .build();
    }
}