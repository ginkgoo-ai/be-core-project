package com.ginkgooai.core.project.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request payload for patch role details")
public class ProjectRolePatchRequest {

    @Schema(description = "Character description")
    private String characterDescription;

    @Schema(description = "Self tape instructions")
    private String selfTapeInstructions;

    @Schema(description = "Array of sides file url")
    private List<String> sides;
}