package com.ginkgooai.core.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Talent search request parameters")
public class TalentSearchRequest {

    @Size(max = 100, message = "Keyword length cannot exceed 100 characters")
    @Schema(description = "Search keyword for fuzzy matching talent name | email | agencyName | agentName", example = "actor")
    private String keyword;
}