package com.ginkgooai.core.project.service.application;

import com.ginkgooai.core.common.bean.ActivityType;
import com.ginkgooai.core.common.exception.ResourceNotFoundException;
import com.ginkgooai.core.common.utils.ActivityLogger;
import com.ginkgooai.core.project.domain.talent.ImdbMovieItem;
import com.ginkgooai.core.project.domain.talent.Talent;
import com.ginkgooai.core.project.domain.talent.TalentStatus;
import com.ginkgooai.core.project.dto.KnownForItem;
import com.ginkgooai.core.project.dto.TalentProfileData;
import com.ginkgooai.core.project.dto.request.TalentCreateRequest;
import com.ginkgooai.core.project.dto.request.TalentSearchRequest;
import com.ginkgooai.core.project.dto.response.TalentResponse;
import com.ginkgooai.core.project.repository.ImdbMovieItemRepository;
import com.ginkgooai.core.project.repository.TalentRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TalentService {

    private final TalentRepository talentRepository;
    private final ImdbMovieItemRepository movieItemRepository;
    private final TalentProfileScraperService profileScraperService;
    private final ActivityLogger activityLogger;

    @Transactional
    public Talent createTalentFromProfiles(TalentCreateRequest request, String userId) {
        // Scrape profiles if URLs are provided
        TalentProfileData imdbProfile = null;
        if (StringUtils.hasText(request.getImdbProfileUrl())) {
            imdbProfile = profileScraperService.scrapeFromImdb(request.getImdbProfileUrl());
        }

        TalentProfileData spotlightProfile = null;
        if (StringUtils.hasText(request.getSpotlightProfileUrl())) {
            spotlightProfile = profileScraperService.scrapeFromSpotlight(request.getSpotlightProfileUrl());
        }

        // Merge profiles with manual input
        Talent talent = mergeTalentData(request, imdbProfile, spotlightProfile);
        talent.setCreatedBy(userId);
        talent.setWorkspaceId(request.getWorkspaceId());

        Talent saved = talentRepository.save(talent);

        // Log activity
        activityLogger.log(
            saved.getWorkspaceId(),
            null,
            saved.getId(),
            ActivityType.TALENT_IMPORTED,
            Map.of(
                "name", saved.getName(),
                "sources", getProfileSources(imdbProfile, spotlightProfile)
            ),
            null,
            userId
        );

        return saved;
    }

    @Transactional
    public Talent refreshTalentProfiles(String talentId, String userId) {
        Talent talent = talentRepository.findById(talentId)
                .orElseThrow(() -> new ResourceNotFoundException("Talent", "id", talentId));

        // Refresh profiles
        TalentProfileData imdbProfile = null;
        if (StringUtils.hasText(talent.getImdbProfileUrl())) {
            imdbProfile = profileScraperService.scrapeFromImdb(talent.getImdbProfileUrl());
        }

        TalentProfileData spotlightProfile = null;
        if (StringUtils.hasText(talent.getSpotlightProfileUrl())) {
            spotlightProfile = profileScraperService.scrapeFromSpotlight(talent.getSpotlightProfileUrl());
        }

        // Update talent with new profile data
        updateTalentFromProfiles(talent, imdbProfile, spotlightProfile);

        return talentRepository.save(talent);
    }

    private void updateTalentFromProfiles(Talent talent, TalentProfileData imdbProfile, TalentProfileData spotlightProfile) {
        talent.setName(imdbProfile != null ? imdbProfile.getName() : 
                      spotlightProfile != null ? spotlightProfile.getName() : talent.getName());
        talent.setProfilePhotoUrl(imdbProfile != null ? imdbProfile.getPhotoUrl() : 
                                 spotlightProfile != null ? spotlightProfile.getPhotoUrl() : talent.getProfilePhotoUrl());
        talent.setImdbProfileUrl(imdbProfile != null ? imdbProfile.getSourceUrl() : talent.getImdbProfileUrl());
        talent.setSpotlightProfileUrl(spotlightProfile != null ? spotlightProfile.getSourceUrl() : talent.getSpotlightProfileUrl());
        talent.setPersonalDetails(imdbProfile != null ? imdbProfile.getPersonalDetails() : talent.getPersonalDetails());
    }

    private Talent mergeTalentData(TalentCreateRequest request, 
                                 TalentProfileData imdbProfile, 
                                 TalentProfileData spotlightProfile) {
        return Talent.builder()
                .name(request.getName() != null ? request.getName() : 
                     imdbProfile != null ? imdbProfile.getName() :
                     spotlightProfile != null ? spotlightProfile.getName() : null)
                .imdbProfileUrl(request.getImdbProfileUrl())
                .spotlightProfileUrl(request.getSpotlightProfileUrl())
                .profilePhotoUrl(request.getProfilePhotoUrl() != null ? request.getProfilePhotoUrl() :
                               imdbProfile != null ? imdbProfile.getPhotoUrl() :
                               spotlightProfile != null ? spotlightProfile.getPhotoUrl() : null)
                .personalDetails(request.getPersonalDetails() != null ? request.getPersonalDetails() :
                                imdbProfile != null ? imdbProfile.getPersonalDetails() : null)
                .status(TalentStatus.ACTIVE)
                .build();
    }


    private String getProfileSources(TalentProfileData imdbProfile, TalentProfileData spotlightProfile) {
        List<String> sources = new ArrayList<>();
        if (imdbProfile != null) sources.add("IMDB");
        if (spotlightProfile != null) sources.add("Spotlight");
        return String.join(", ", sources);
    }

    public TalentResponse getTalentById(String id) {
        Talent talent = talentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Talent", "id", id));

        TalentResponse talentResponse = TalentResponse.from(talent);
        talentResponse.setKnownFor(getKnownForMovies(id).stream().map(KnownForItem::from).collect(Collectors.toSet()));
    
        return talentResponse;
    }

    public Page<TalentResponse> searchTalents(TalentSearchRequest request, Pageable pageable) {
        return talentRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.getWorkspaceId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("workspaceId"), request.getWorkspaceId()));
            }

            if (StringUtils.hasText(request.getKeyword())) {
                String keyword = "%" + request.getKeyword().toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), keyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), keyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("agencyName")), keyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("agentName")), keyword)
                ));
            }

            if (StringUtils.hasText(request.getAgencyName())) {
                predicates.add(criteriaBuilder.equal(root.get("agencyName"), request.getAgencyName()));
            }

            if (StringUtils.hasText(request.getAgentName())) {
                predicates.add(criteriaBuilder.equal(root.get("agentName"), request.getAgentName()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }, pageable).map(TalentResponse::from);
    }

    public List<ImdbMovieItem> getKnownForMovies(String talentId) {
        Talent talent = talentRepository.findById(talentId)
                .orElseThrow(() -> new EntityNotFoundException("Talent not found"));

        if (talent.getKnownForMovieIds() == null || talent.getKnownForMovieIds().length == 0) {
            return Collections.emptyList();
        }

        return movieItemRepository.findAllById(Arrays.asList(talent.getKnownForMovieIds()));
    }
}