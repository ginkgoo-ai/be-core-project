package com.ginkgooai.core.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Request object for creating a new submission")
public class SubmissionCreateRequest {
    
    @Schema(description = "Application ID this submission belongs to", required = true)
    @NotBlank(message = "Application ID is required")
    private String applicationId;

    @Schema(description = "Id of the uploaded video", required = true)
    @NotBlank(message = "Video file id is required")
    private String videoId;
}