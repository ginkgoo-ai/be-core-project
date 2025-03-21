package com.ginkgooai.core.project.dto.response;

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing batch share shortlist results")
public class BatchShareShortlistResponse {

    @Schema(description = "Map of recipient emails to their respective access links", example = "{\"user1@example.com\": \"https://example.com/link1\", \"user2@example.com\": \"https://example.com/link2\"}")
    private Map<String, String> shareLinks;

    @Schema(description = "Number of recipients successfully processed", example = "5")
    private int successCount;

    @Schema(description = "Status message", example = "Successfully shared with 5 recipients")
    private String message;

    public static BatchShareShortlistResponse from(Map<String, String> shareLinks) {
        return BatchShareShortlistResponse.builder()
                .shareLinks(shareLinks)
                .successCount(shareLinks.size())
                .message("Successfully shared with " + shareLinks.size() + " recipient(s)")
                .build();
    }
}