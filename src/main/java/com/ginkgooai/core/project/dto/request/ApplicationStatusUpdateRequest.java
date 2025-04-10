package com.ginkgooai.core.project.dto.request;

import com.ginkgooai.core.project.domain.application.ApplicationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update application status")
public class ApplicationStatusUpdateRequest {

    @NotNull(message = "Status cannot be null")
    @Schema(description = "New status for the application:REVIEWED, DECLINED, CAST", required = true, example = "CAST")
    private ApplicationStatus status;
}