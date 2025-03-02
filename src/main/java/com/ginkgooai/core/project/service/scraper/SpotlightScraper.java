package com.ginkgooai.core.project.service.scraper;

import com.ginkgooai.core.project.dto.TalentProfileData;
import com.ginkgooai.core.project.repository.TalentProfileMetaRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class SpotlightScraper {
    
    private final TalentProfileMetaRepository talentProfileMetaRepository;

    public TalentProfileData scrapeProfile(String spotlightUrl) throws Exception {
        Document doc = Jsoup.connect(spotlightUrl)
                .userAgent("Mozilla/5.0")
                .get();

        String name = doc.select("h1.name").text();
        
        String photoUrl = doc.select("img.profile-photo").attr("src");

        return TalentProfileData.builder()
                .name(name)
                .photoUrl(photoUrl)
                .source("Spotlight")
                .sourceUrl(spotlightUrl)
                .build();
    }
}