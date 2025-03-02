package com.ginkgooai.core.project.dto.response;

import com.ginkgooai.core.project.dto.KnownForItem;
import com.ginkgooai.core.project.dto.TalentProfileData;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Data
@Builder
@Schema(description = "Talent profile data response from scraping services")
public class TalentProfileResponse {

    @Schema(description = "Full name of the talent", 
            example = "Tom Cruise")
    private String name;

    @Schema(description = "Additional name information including birth/death years", 
            example = "1962-, Los Angeles, California, USA")
    private String nameSuffix;

    @Schema(description = "List of languages the talent speaks", 
            example = "[\"English\", \"French\", \"Spanish\"]")
    private Set<String> languages;

    @Schema(description = "Notable works the talent is known for")
    private Set<KnownForItem> knownFor;

    @Schema(description = "URL to the talent's profile photo", 
            example = "https://m.media-amazon.com/images/M/profile.jpg")
    private String photoUrl;

    @Schema(description = "Personal details of the talent including birth date, height, etc.")
    private Map<String, String> personalDetails;

    @Schema(description = "Source of the profile data", 
            example = "IMDB", 
            allowableValues = {"IMDB", "Spotlight"})
    private String source;

    @Schema(description = "Original URL where the data was scraped from", 
            example = "https://www.imdb.com/name/nm0000129/")
    private String sourceUrl;

    @Schema(description = "Timestamp when the data was fetched", 
            example = "2025-03-01T15:19:21.119Z")
    private LocalDateTime fetchedAt;

    @Schema(description = "Additional data specific to the source")
    private Map<String, Object> additionalData;
    public static TalentProfileResponse fromTalentProfileData(TalentProfileData data) {
        Set<KnownForItem> knownForItems = null;
        if (data.getKnownFor() != null) {
            knownForItems = data.getKnownFor().stream()
                    .map(KnownForItem::from)
                    .collect(java.util.stream.Collectors.toSet());
        }

        return TalentProfileResponse.builder()
                .name(data.getName())
                .nameSuffix(data.getNameSuffix())
                .languages(data.getLanguages())
                .knownFor(knownForItems)
                .photoUrl(data.getPhotoUrl())
                .personalDetails(data.getPersonalDetails())
                .source(data.getSource())
                .sourceUrl(data.getSourceUrl())
                .fetchedAt(data.getFetchedAt())
                .additionalData(data.getAdditionalData())
                .build();
    }
}