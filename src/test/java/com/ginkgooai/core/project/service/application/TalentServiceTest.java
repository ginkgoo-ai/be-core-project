package com.ginkgooai.core.project.service.application;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ginkgooai.core.common.bean.ActivityType;
import com.ginkgooai.core.common.exception.InternalServiceException;
import com.ginkgooai.core.common.exception.ResourceNotFoundException;
import com.ginkgooai.core.project.domain.talent.Talent;
import com.ginkgooai.core.project.domain.talent.TalentProfileMeta;
import com.ginkgooai.core.project.domain.talent.TalentStatus;
import com.ginkgooai.core.project.dto.TalentProfileData;
import com.ginkgooai.core.project.dto.request.TalentRequest;
import com.ginkgooai.core.project.repository.ImdbMovieItemRepository;
import com.ginkgooai.core.project.repository.TalentRepository;
import com.ginkgooai.core.project.service.ActivityLoggerService;

@ExtendWith(MockitoExtension.class)
public class TalentServiceTest {

    @Mock
    private TalentRepository talentRepository;

    @Mock
    private ImdbMovieItemRepository movieItemRepository;

    @Mock
    private TalentProfileScraperService profileScraperService;

    @Mock
    private ActivityLoggerService activityLogger;

    @InjectMocks
    private TalentService talentService;

    private TalentRequest talentRequest;
    private Talent talent;
    private TalentProfileMeta imdbProfile;
    private TalentProfileMeta spotlightProfile;
    private TalentProfileData imdbProfileData;
    private TalentProfileData spotlightProfileData;
    private String workspaceId = "workspace-1";
    private String userId = "user-1";

    @BeforeEach
    void setUp() {
        // Setup basic talent request
        talentRequest = new TalentRequest();
        talentRequest.setName("John Doe");
        talentRequest.setEmail("john@example.com");
        talentRequest.setImdbProfileUrl("https://imdb.com/name/nm1234567/");
        talentRequest.setSpotlightProfileUrl("https://www.spotlight.com/1234567/");

        // Setup talent
        talent = Talent.builder()
                .id("talent-1")
                .name("John Doe")
                .email("john@example.com")
                .imdbProfileUrl("https://imdb.com/name/nm1234567/")
                .spotlightProfileUrl("https://www.spotlight.com/1234567/")
                .status(TalentStatus.ACTIVE)
                .build();

        // Setup profile data
        imdbProfileData = TalentProfileData.builder()
                .name("John Doe")
                .photoUrl("https://example.com/photo.jpg")
                .sourceUrl("https://imdb.com/name/nm1234567/")
                .source("IMDB")
                .personalDetails(new HashMap<>())
                .build();

        spotlightProfileData = TalentProfileData.builder()
                .name("John Doe")
                .photoUrl("https://example.com/photo2.jpg")
                .sourceUrl("https://www.spotlight.com/1234567/")
                .source("Spotlight")
                .build();

        // Setup profile metas
        imdbProfile = TalentProfileMeta.builder()
                .id("profile-1")
                .source("IMDB")
                .sourceUrl("https://imdb.com/name/nm1234567/")
                .data(imdbProfileData)
                .build();

        spotlightProfile = TalentProfileMeta.builder()
                .id("profile-2")
                .source("Spotlight")
                .sourceUrl("https://www.spotlight.com/1234567/")
                .data(spotlightProfileData)
                .build();
    }

    @Test
    void createTalentFromProfiles_WithoutProfileUrls() {
        // Setup request without profile URLs
        TalentRequest request = new TalentRequest();
        request.setName("John Doe");
        request.setEmail("john@example.com");

        // Set a profile object to bypass scraping
//        request.setProfile(imdbProfileData);

        // Mock repository
        when(talentRepository.save(any(Talent.class))).thenReturn(talent);

        // Execute test
        Talent result = talentService.createTalentFromProfiles(request, workspaceId, userId);

        // Verify result
        assertNotNull(result);

        // Verify scraper was NOT called
        verify(profileScraperService, never()).scrapeFromImdb(anyString());
        verify(profileScraperService, never()).scrapeFromSpotlight(anyString());
    }

    void createTalentFromProfiles_ImdbScraperFailure() {
        // Mock IMDB scraper to fail
        when(profileScraperService.scrapeFromImdb(anyString()))
                .thenThrow(new InternalServiceException("Failed to load IMDB profile"));

        // Mock Spotlight scraper to succeed
        when(profileScraperService.scrapeFromSpotlight(anyString())).thenReturn(spotlightProfile);

        // Execute test and expect exception
        Exception exception = assertThrows(InternalServiceException.class, () -> {
            talentService.createTalentFromProfiles(talentRequest, workspaceId, userId);
        });

        // Verify exception details
        assertTrue(exception.getMessage().contains("Failed to load IMDB profile"));

        // Verify scraper calls
        verify(profileScraperService).scrapeFromImdb(eq(talentRequest.getImdbProfileUrl()));

        // Verify no talent was saved
        verify(talentRepository, never()).save(any(Talent.class));
    }

    @Test
    void createTalentFromProfiles_Performance() throws Exception {
        // Mock scrapers with delays to simulate slow web requests
        when(profileScraperService.scrapeFromImdb(anyString())).thenAnswer(invocation -> {
            Thread.sleep(500); // Simulate network delay
            return imdbProfile;
        });

        when(profileScraperService.scrapeFromSpotlight(anyString())).thenAnswer(invocation -> {
            Thread.sleep(500); // Simulate network delay
            return spotlightProfile;
        });

        // Mock repository
        when(talentRepository.save(any(Talent.class))).thenReturn(talent);

        // Execute test with timing
        long startTime = System.currentTimeMillis();
        talentService.createTalentFromProfiles(talentRequest, workspaceId, userId);
        long endTime = System.currentTimeMillis();

        // Verify execution time (should be at least 1 second due to simulated delays)
        assertTrue((endTime - startTime) >= 1000, "Method should take at least 1 second due to scraper delays");

        // Verify both scrapers were called (sequentially, causing delay)
        verify(profileScraperService).scrapeFromImdb(anyString());
        verify(profileScraperService).scrapeFromSpotlight(anyString());
    }

    @Test
    void updateTalent_Success() {
        // Mock repository
        when(talentRepository.findById(anyString())).thenReturn(Optional.of(talent));
        when(profileScraperService.scrapeFromImdb(anyString())).thenReturn(imdbProfile);
        when(profileScraperService.scrapeFromSpotlight(anyString())).thenReturn(spotlightProfile);
        when(talentRepository.save(any(Talent.class))).thenReturn(talent);

        // Execute test
        Talent result = talentService.updateTalent(talentRequest, talent.getId());

        // Verify result
        assertNotNull(result);

        // Verify repository and scraper calls
        verify(talentRepository).findById(eq(talent.getId()));
        verify(profileScraperService).scrapeFromImdb(anyString());
        verify(profileScraperService).scrapeFromSpotlight(anyString());
        verify(talentRepository).save(eq(talent));
    }

    @Test
    void updateTalent_TalentNotFound() {
        // Mock repository to return empty
        when(talentRepository.findById(anyString())).thenReturn(Optional.empty());

        // Execute test and expect exception
        assertThrows(ResourceNotFoundException.class, () -> {
            talentService.updateTalent(talentRequest, "non-existent-id");
        });

        // Verify no further calls
        verify(profileScraperService, never()).scrapeFromImdb(anyString());
        verify(profileScraperService, never()).scrapeFromSpotlight(anyString());
        verify(talentRepository, never()).save(any(Talent.class));
    }

    @Test
    void updateTalent_ScraperFailure() {
        // Mock repository
        when(talentRepository.findById(anyString())).thenReturn(Optional.of(talent));

        // Mock IMDB scraper to fail
        when(profileScraperService.scrapeFromImdb(anyString()))
                .thenThrow(new InternalServiceException("Failed to load IMDB profile"));

        // Execute test and expect exception
        Exception exception = assertThrows(InternalServiceException.class, () -> {
            talentService.updateTalent(talentRequest, talent.getId());
        });

        // Verify exception details
        assertTrue(exception.getMessage().contains("Failed to load IMDB profile"));

        // Verify repository calls but no save
        verify(talentRepository).findById(eq(talent.getId()));
        verify(talentRepository, never()).save(any(Talent.class));
    }
}