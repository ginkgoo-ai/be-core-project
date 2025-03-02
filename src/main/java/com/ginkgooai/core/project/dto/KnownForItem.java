package com.ginkgooai.core.project.dto;

import com.ginkgooai.core.project.domain.talent.ImdbMovieItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Details about a notable work")
public class KnownForItem {
    @Schema(description = "Title of the work",
            example = "Top Gun: Maverick")
    private String title;

    @Schema(description = "Cover of the work",
            example = "https://example.com/covers/top-gun-maverick.jpg")
    private String cover;

    @Schema(description = "Role played by the talent",
            example = "Captain Pete 'Maverick' Mitchell")
    private String role;

    @Schema(description = "Year of release",
            example = "2022")
    private String year;

    @Schema(description = "Rating of the work",
            example = "8.3")
    private String rating;

    @Schema(description = "Type of media",
            example = "Movie")
    private String mediaType;

    @Schema(description = "URL to the work's page",
            example = "/title/tt1745960/")
    private String titleUrl;

    public static KnownForItem from(ImdbMovieItem item) {
        return KnownForItem.builder()
                .title(item.getTitle())
                .role(item.getRole())
                .year(item.getYear())
                .rating(item.getRating())
                .mediaType(item.getMediaType())
                .titleUrl(item.getTitleUrl())
                .build();
    }
}

