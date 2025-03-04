package com.ginkgooai.core.project.dto.request;

import com.ginkgooai.core.project.dto.TalentProfileData;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Request object for creating/update a new talent profile")
public class TalentRequest {

    @Schema(description = "Full name of the talent",
            example = "John Smith",
            required = true)
    @NotBlank(message = "Name is required")
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

    @Schema(description = "Name of the talent's agency",
            example = "Creative Artists Agency",
            required = true)
    private String agencyName;

    @Schema(description = "Name of the talent's agent",
            example = "Jane Wilson")
    private String agentName;

    @Schema(description = "Contact email of the talent's agent",
            example = "jane.wilson@caa.com",
            required = true)
    @Email(message = "Must be a valid email address")
    private String agentEmail;
   
    @Schema(description = "Additional personal details about the talent")
    private TalentProfileData profile;
}