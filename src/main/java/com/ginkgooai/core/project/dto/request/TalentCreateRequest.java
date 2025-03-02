package com.ginkgooai.core.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
@Schema(description = "Request object for creating a new talent profile")
public class TalentCreateRequest {

    @Schema(description = "Workspace identifier where the talent will be created",
            example = "ws_12345678",
            required = true)
    @NotBlank(message = "Workspace ID is required")
    private String workspaceId;

    @Schema(description = "Full name of the talent",
            example = "John Smith",
            required = true)
    @NotBlank(message = "Name is required")
    private String name;

    @Schema(description = "Name suffix (birth/death years, birthplace, etc., distinct from the name)",
            example = "1940-2010",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String nameSuffix;

    @Schema(description = "Contact email address of the talent",
            example = "john.smith@example.com")
    @Email(message = "Must be a valid email address")
    private String email;

    @Schema(description = "IMDB profile URL for talent information scraping",
            example = "https://www.imdb.com/name/nm0000123/",
            pattern = "^https?://(?:www\\.)?imdb\\.com/name/[a-zA-Z0-9]+/?.*$")
    @Pattern(regexp = "^https?://(?:www\\.)?imdb\\.com/name/[a-zA-Z0-9]+/?.*$",
            message = "Must be a valid IMDB profile URL")
    private String imdbProfileUrl;

    @Schema(description = "Spotlight profile URL for talent information scraping",
            example = "https://www.spotlight.com/1234-5678-9012-3456/",
            pattern = "^https?://(?:www\\.)?spotlight\\.com/\\d{4}-\\d{4}-\\d{4}-\\d{4}/?.*$")
    @Pattern(regexp = "^https?://(?:www\\.)?spotlight\\.com/\\d{4}-\\d{4}-\\d{4}-\\d{4}/?.*$",
            message = "Must be a valid Spotlight profile URL")
    private String spotlightProfileUrl;

    @Schema(description = "URL of the talent's profile photo",
            example = "https://example.com/photos/profile.jpg")
    private String profilePhotoUrl;

    @Schema(description = "List of IMDB movie IDs that the talent is known for",
            example = "tt1234567, tt2345678")
    private String[] knownForMovieIds;

    @Schema(description = "Name of the talent's agency",
            example = "Creative Artists Agency")
    private String agencyName;

    @Schema(description = "Name of the talent's agent",
            example = "Jane Wilson")
    private String agentName;

    @Schema(description = "Contact email of the talent's agent",
            example = "jane.wilson@caa.com")
    @Email(message = "Must be a valid email address")
    private String agentEmail;

    @Schema(description = "Additional attributes for the talent profile. Can include custom fields like skills, preferences, etc.",
            example = """
                    {
                        "height": "180cm",
                        "eyeColor": "blue",
                        "languages": ["English", "French"],
                        "skills": ["Stage Combat", "Horse Riding"]
                    }
                    """)
    private Map<String, Object> attributes;

    @Schema(description = "Personal details of the talent. Typically includes physical characteristics and biographical information.",
            example = """
                    {
                        "dateOfBirth": "1990-01-01",
                        "nationality": "British",
                        "gender": "Male",
                        "height": "180cm",
                        "weight": "75kg"
                    }
                    """)
    private Map<String, String> personalDetails;
}