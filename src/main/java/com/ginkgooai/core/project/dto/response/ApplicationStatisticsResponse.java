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
public class ApplicationStatisticsResponse {

    @Schema(description = "Count of applications with ADDED status", example = "100")
    private Long added;

    @Schema(description = "Count of applications with NDA_SENT status", example = "80")
    private Long ndaSent;

    @Schema(description = "Count of applications with NDA_SIGNED status", example = "69")
    private Long ndaSigned;

    @Schema(description = "Count of applications with REQUESTED status", example = "40")
    private Long requested;

    @Schema(description = "Count of applications with DECLINED status", example = "10")
    private Long declined;

    @Schema(description = "Count of applications with SUBMITTED status", example = "30")
    private Long submitted;

    @Schema(description = "Count of applications with REVIEWED status", example = "20")
    private Long reviewed;

    @Schema(description = "Count of applications with RETAPE status", example = "10")
    private Long retape;

    @Schema(description = "Count of applications with SHORTLISTED status", example = "10")
    private Long shortlisted;

    /**
     * Create response from a map of status counts
     *
     * @param statusCounts Map of ApplicationStatus to count
     * @return ApplicationStatusCountResponse with populated counts
     */
    public static ApplicationStatisticsResponse from(Map<ApplicationStatus, Long> statusCounts) {
        return ApplicationStatisticsResponse.builder()
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