package com.ginkgooai.core.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "Request object for adding a video to shortlist")
public class ShortlistItemAddRequest {

    @Schema(description = "Video submission identifier", 
            example = "vid_12345",
            required = true)
    @NotBlank(message = "Video submission ID is required")
    private List<String> videoSubmissionId;

    @Schema(description = "Notes about the video", 
            example = "Great performance, particularly strong in emotional scenes")
    private String notes;
}