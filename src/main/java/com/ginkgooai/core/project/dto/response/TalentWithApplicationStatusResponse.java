package com.ginkgooai.core.project.dto.response;

import com.ginkgooai.core.project.domain.application.ApplicationStatus;
import com.ginkgooai.core.project.domain.talent.Talent;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Response object containing talent information with application status for a specific role")
public class TalentWithApplicationStatusResponse {
    @Schema(description = "Unique identifier for the talent")
    private String id;

    @Schema(description = "User's name")
    private String name;

    @Schema(description = "Contact email address")
    private String email;

    @Schema(description = "URL of the talent's profile photo")
    private String profilePhotoUrl;

    @Schema(description = "Current status of the talent (e.g., ACTIVE, INACTIVE)")
    private String status;

    @Schema(description = "Application status for the specific role, null if not applied")
    private ApplicationStatus applicationStatus;

    @Schema(description = "Flag indicating if the talent has applied for the role")
    private boolean hasApplied;

    @Schema(description = "Application ID if the talent has applied for the role")
    private String applicationId;

    public static TalentWithApplicationStatusResponse from(Talent talent, ApplicationStatus status,
                                                           boolean hasApplied, String applicationId) {
        return TalentWithApplicationStatusResponse.builder()
            .id(talent.getId())
            .name(talent.getName())
            .email(talent.getEmail())
            .profilePhotoUrl(talent.getProfilePhotoUrl())
            .status(talent.getStatus().name())
            .applicationStatus(status)
            .hasApplied(hasApplied)
            .applicationId(applicationId)
            .build();
    }
}