package com.ginkgooai.core.project.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ginkgooai.core.project.domain.talent.Talent;
import com.ginkgooai.core.project.domain.talent.TalentProfileMeta;
import com.ginkgooai.core.project.dto.TalentProfileData;
import com.ginkgooai.core.project.dto.response.TalentProfileResponse;
import com.ginkgooai.core.project.service.scraper.ImdbScraper;
import com.ginkgooai.core.project.service.scraper.SpotlightScraper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/talent-scraper")
@Tag(name = "Talent Scraper", description = "APIs for scraping talent profiles")
@RequiredArgsConstructor
public class TalentScraperController {

        private final ImdbScraper imdbScraper;
        private final SpotlightScraper spotlightScraper;

        @Operation(summary = "Scrape talent profile from IMDB", description = "Fetches talent information from IMDB URL and saves it to database")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully scraped and saved talent profile", content = @Content(schema = @Schema(implementation = Talent.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid IMDB URL provided"),
                        @ApiResponse(responseCode = "404", description = "Profile not found on IMDB"),
                        @ApiResponse(responseCode = "500", description = "Internal server error during scraping")
        })
        @PostMapping("/imdb")
        public ResponseEntity<TalentProfileResponse> scrapeImdbProfile(
                        @Parameter(description = "IMDB profile URL", required = true, example = "https://www.imdb.com/name/nm0000614/") @RequestParam String imdbUrl) {
                try {
                        TalentProfileMeta profile = imdbScraper.scrapeProfile(imdbUrl);
                        return ResponseEntity.ok(TalentProfileResponse.fromTalentProfileData(profile.getData()));
                } catch (Exception e) {
                        log.error("Error scraping IMDB profile: {}", imdbUrl, e);
                        throw new RuntimeException("Failed to scrape IMDB profile: " + e.getMessage());
                }
        }

        @Operation(summary = "Scrape talent profile from Spotlight", description = "Fetches talent information from Spotlight URL and saves it to database")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully scraped and saved talent profile", content = @Content(schema = @Schema(implementation = Talent.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid Spotlight URL provided"),
                        @ApiResponse(responseCode = "404", description = "Profile not found on Spotlight"),
                        @ApiResponse(responseCode = "500", description = "Internal server error during scraping")
        })
        @PostMapping("/spotlight")
        public ResponseEntity<TalentProfileResponse> scrapeSpotlightProfile(
                        @Parameter(description = "Spotlight profile URL", required = true, example = "https://www.spotlight.com/profile/1234-5678-9012-3456") @RequestParam String spotlightUrl) {
                try {
                        TalentProfileData profileData = spotlightScraper.scrapeProfile(spotlightUrl).getData();
                        return ResponseEntity.ok(TalentProfileResponse.fromTalentProfileData(profileData));
                } catch (Exception e) {
                        log.error("Error scraping Spotlight profile: {}", spotlightUrl, e);
                        throw new RuntimeException("Failed to scrape Spotlight profile: " + e.getMessage());
                }
        }
}