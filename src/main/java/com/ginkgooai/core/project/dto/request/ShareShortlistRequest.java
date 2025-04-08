package com.ginkgooai.core.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Builder
@Schema(description = "Request for sharing multiple submissions with multiple recipients")
public class ShareShortlistRequest {

	@NotEmpty(message = "Product ID cannot be empty")
	@Schema(description = "Project identifier associated with this submission", example = "project-456")
	private String projectId;

    @NotEmpty(message = "Redirect url cannot be empty")
    @Schema(description = "Redirect URL after user clicks the link", example = "https://www.slate.com/shared-shortlist")
    private String redirectUrl;

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
        @Schema(description = "Email address of public recipient", example = "recipient@slate.com")
        private String email;

        @NotBlank
        @Schema(description = "First name of public recipient", example = "Recipient")
        private String firstName;

        @NotBlank
        @Schema(description = "Last name of public recipient", example = "Recipient")
        private String lastName;
    }
}