package com.ginkgooai.core.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "Request object for reordering shortlist items")
public class ShortlistReorderRequest {

    @Schema(description = "Ordered list of shortlist item IDs", 
            example = "[\"sli_1\", \"sli_2\", \"sli_3\"]",
            required = true)
    @NotEmpty(message = "Item IDs list cannot be empty")
    private List<String> itemIds;
}