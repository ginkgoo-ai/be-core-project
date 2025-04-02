package com.ginkgooai.core.project.dto.request;

import com.ginkgooai.core.project.domain.talent.Contact;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for creating/update a new talent profile")
public class TalentRequest {
    @Schema(description = "User's full name")
    private String name;

    @Schema(description = "Name suffix (birth/death years, birthplace, etc., distinct from the name)",
            example = "1940-2010")
    private String nameSuffix;

    @Schema(description = "Contact email address of the talent",
            example = "john.smith@example.com")
    @Email(message = "Must be a valid email address")
    private String email;

    @Schema(description = "IMDB profile URL for talent information scraping",
            example = "https://www.imdb.com/name/nm0000123/",
            pattern = "^https?://(?:www\\.)?imdb\\.com/.*$")
    @Pattern(regexp = "^https?://(?:www\\.)?imdb\\.com/.*$",
            message = "Must be a valid IMDB URL")
    private String imdbProfileUrl;

    @Schema(description = "Spotlight profile URL for talent information scraping",
            example = "https://www.spotlight.com/1234-5678-9012-3456/",
            pattern = "^https?://(?:www\\.)?spotlight\\.com/.*$")
    @Pattern(regexp = "^https?://(?:www\\.)?spotlight\\.com/.*$",
            message = "Must be a valid Spotlight URL")
    private String spotlightProfileUrl;

    @Schema(description = "URL of the talent's profile photo",
            example = "https://example.com/photos/profile.jpg",
            required = true)
    private String profilePhotoUrl;

    @Schema(description = "List of contact persons associated with the talent (agents, managers, etc.)")
    private List<Contact> contacts;
}