package com.ginkgooai.core.project.dto.response;

import com.ginkgooai.core.project.domain.application.ApplicationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing counts of applications by status")
public class ApplicationStatusCountResponse {

    @Schema(description = "Count of applications with ADDED status")
    private Long added;

    @Schema(description = "Count of applications with NDA_SENT status")
    private Long ndaSent;

    @Schema(description = "Count of applications with NDA_SIGNED status")
    private Long ndaSigned;

    @Schema(description = "Count of applications with REQUESTED status")
    private Long requested;

    @Schema(description = "Count of applications with DECLINED status")
    private Long declined;

    @Schema(description = "Count of applications with SUBMITTED status")
    private Long submitted;

    @Schema(description = "Count of applications with REVIEWED status")
    private Long reviewed;

    @Schema(description = "Count of applications with RETAPE status")
    private Long retape;

    @Schema(description = "Count of applications with SHORTLISTED status")
    private Long shortlisted;

    /**
     * Create response from a map of status counts
     *
     * @param statusCounts Map of ApplicationStatus to count
     * @return ApplicationStatusCountResponse with populated counts
     */
    public static ApplicationStatusCountResponse from(Map<ApplicationStatus, Long> statusCounts) {
        return ApplicationStatusCountResponse.builder()
            .added(statusCounts.getOrDefault(ApplicationStatus.ADDED, 0L))
            .ndaSent(statusCounts.getOrDefault(ApplicationStatus.NDA_SENT, 0L))
            .ndaSigned(statusCounts.getOrDefault(ApplicationStatus.NDA_SIGNED, 0L))
            .requested(statusCounts.getOrDefault(ApplicationStatus.REQUESTED, 0L))
            .declined(statusCounts.getOrDefault(ApplicationStatus.DECLINED, 0L))
            .submitted(statusCounts.getOrDefault(ApplicationStatus.SUBMITTED, 0L))
            .reviewed(statusCounts.getOrDefault(ApplicationStatus.REVIEWED, 0L))
            .retape(statusCounts.getOrDefault(ApplicationStatus.RETAPE, 0L))
            .shortlisted(statusCounts.getOrDefault(ApplicationStatus.SHORTLISTED, 0L))
            .build();
    }
}