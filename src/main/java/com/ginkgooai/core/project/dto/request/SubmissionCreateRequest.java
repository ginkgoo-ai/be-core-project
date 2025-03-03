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

    @Schema(description = "URL of the video", required = true)
    @NotBlank(message = "Video URL is required")
    private String videoUrl;

    @Schema(description = "URL of the video thumbnail")
    private String videoThumbnailUrl;

    @Schema(description = "Duration of the video in seconds")
    private Long videoDuration;

    @Schema(description = "Resolution of the video (e.g., 1920x1080)")
    private String videoResolution;
}