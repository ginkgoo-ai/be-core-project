package com.ginkgooai.core.project.domain.talent;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Contact person information")
public class Contact {
    @Schema(description = "Full name of the contact person", example = "Jane Wilson")
    private String fullName;

    @Schema(description = "Email address of the contact person", example = "jane.wilson@caa.com")
    @Email(message = "Must be a valid email address")
    private String email;

    @Schema(
        description = "Country code for phone number",
        example = "+1",
        pattern = "^\\+[1-9]\\d{0,2}$"
    )
    @Pattern(
        regexp = "^\\+[1-9]\\d{0,2}$",
        message = "Country code must start with '+' followed by 1-3 digits"
    )
    private String countryCode;

    @Schema(
        description = "Phone number without country code",
        example = "(020) 1234 5678",
        pattern = "^[\\d\\s\\(\\)\\-]{6,20}$"
    )
    @Pattern(
        regexp = "^[\\d\\s\\(\\)\\-]{6,20}$",
        message = "Phone number must contain 6-20 characters (digits, spaces, parentheses, or hyphens)"
    )
    private String phoneNumber;

    @Schema(
        description = "Role of the contact person",
        example = "Agent",
        allowableValues = {"Agent", "Agency", "Talent", "Other"}
    )
    private String role;
}