package com.ginkgooai.core.project.dto;

import com.ginkgooai.core.project.domain.talent.ImdbMovieItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Data
@Builder
@Schema(description = "Talent profile data containing information scraped from IMDB or Spotlight")
public class TalentProfileData {

    @Schema(description = "The talent's name")
    private String name;

    @Schema(description = "Name suffix (birth/death years, birthplace, etc., distinct from the name)")
    private String nameSuffix;

    @Schema(description = "List of languages the talent speaks")
    private Set<String> languages;

    @Schema(description = "List of notable works/credits")
    private Set<ImdbMovieItem> knownFor;

    @Schema(description = "Profile photo URL")
    private String photoUrl;

    @Schema(description = "Personal details including age, height, etc.")
    private Map<String, String> personalDetails;

    @Schema(description = "Data source identifier", allowableValues = {"IMDB", "Spotlight"})
    private String source;

    @Schema(description = "Original profile page URL")
    private String sourceUrl;

    @Schema(description = "Timestamp when the data was fetched")
    private LocalDateTime fetchedAt;

    @Schema(description = "Additional supplementary data")
    private Map<String, Object> additionalData;
}