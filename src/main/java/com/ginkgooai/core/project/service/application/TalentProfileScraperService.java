package com.ginkgooai.core.project.service.application;

import com.ginkgooai.core.common.exception.InternalServiceException;
import com.ginkgooai.core.project.dto.TalentProfileData;
//import com.ginkgooai.core.project.service.scraper.ImdbScraper;
import com.ginkgooai.core.project.service.scraper.SpotlightScraper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TalentProfileScraperService {

//    private final ImdbScraper imdbScraper;
    private final SpotlightScraper spotlightScraper;
//    private final RestTemplate restTemplate;

    public TalentProfileData scrapeFromImdb(String imdbUrl) {
        try {
            log.info("Scraping talent profile from IMDB: {}", imdbUrl);
//            return imdbScraper.scrapeProfile(imdbUrl);
            return null;
        } catch (Exception e) {
            log.error("Failed to scrape IMDB profile: {}", imdbUrl, e);
            throw new InternalServiceException("Failed to load IMDB profile");
        }
    }

    public TalentProfileData scrapeFromSpotlight(String spotlightUrl) {
        try {
            log.info("Scraping talent profile from Spotlight: {}", spotlightUrl);
            return spotlightScraper.scrapeProfile(spotlightUrl);
        } catch (Exception e) {
            log.error("Failed to scrape Spotlight profile: {}", spotlightUrl, e);
            throw new InternalServiceException("Failed to load Spotlight profile");
        }
    }
}