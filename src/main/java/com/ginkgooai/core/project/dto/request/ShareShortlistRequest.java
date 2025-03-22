package com.ginkgooai.core.project.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Builder
@Schema(description = "Request for sharing multiple submissions with multiple recipients")
public class ShareShortlistRequest {

    @NotEmpty
    @Schema(description = "List of submission IDs to share - all submissions will be accessible via a single link for each recipient", example = "[\"submission-123\", \"submission-456\", \"submission-789\"]")
    private List<String> submissionIds;

    @NotEmpty
    @Schema(description = "List of recipient")
    private List<Recipient> recipients;

    @Schema(description = "Number of days before the shared link expires", example = "7")
    private Integer expiresInDays;

    @Getter
    @Setter
    @Builder
    public static class Recipient {
        @Email
        @NotBlank
        @Schema(description = "Email address of the recipient", example = "recipient@slate.com")
        private String email;

        @NotBlank
        @Schema(description = "Name of the recipient", example = "Recipient")
        private String name;
    }
}