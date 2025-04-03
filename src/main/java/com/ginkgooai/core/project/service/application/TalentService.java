package com.ginkgooai.core.project.service.application;

import com.ginkgooai.core.common.exception.ResourceNotFoundException;
import com.ginkgooai.core.project.domain.talent.ImdbMovieItem;
import com.ginkgooai.core.project.domain.talent.Talent;
import com.ginkgooai.core.project.domain.talent.TalentProfileMeta;
import com.ginkgooai.core.project.domain.talent.TalentStatus;
import com.ginkgooai.core.project.dto.KnownForItem;
import com.ginkgooai.core.project.dto.TalentProfileData;
import com.ginkgooai.core.project.dto.request.TalentRequest;
import com.ginkgooai.core.project.dto.request.TalentSearchRequest;
import com.ginkgooai.core.project.dto.response.ApplicationBriefResponse;
import com.ginkgooai.core.project.dto.response.SubmissionBriefResponse;
import com.ginkgooai.core.project.dto.response.TalentBasicResponse;
import com.ginkgooai.core.project.dto.response.TalentResponse;
import com.ginkgooai.core.project.repository.ApplicationRepository;
import com.ginkgooai.core.project.repository.ImdbMovieItemRepository;
import com.ginkgooai.core.project.repository.SubmissionRepository;
import com.ginkgooai.core.project.repository.TalentRepository;
import com.ginkgooai.core.project.service.ActivityLoggerService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TalentService {

    private final TalentRepository talentRepository;
    private final ApplicationRepository applicationRepository;
    private final SubmissionRepository submissionRepository;
    private final ImdbMovieItemRepository movieItemRepository;
    private final TalentProfileScraperService profileScraperService;
    private final ActivityLoggerService activityLogger;

    @Transactional
    public Talent createTalentFromProfiles(TalentRequest request) {
        // Scrape profiles if URLs are provided
        TalentProfileMeta imdbProfile = null;
        if (StringUtils.hasText(request.getImdbProfileUrl())) {
            imdbProfile = profileScraperService.scrapeFromImdb(request.getImdbProfileUrl());
        }

        TalentProfileMeta spotlightProfile = null;
        if (StringUtils.hasText(request.getSpotlightProfileUrl())) {
            spotlightProfile = profileScraperService.scrapeFromSpotlight(request.getSpotlightProfileUrl());
        }

        // Merge profiles with manual input
//        Talent talent = mergeTalentData(request, imdbProfile.getData(), spotlightProfile.getData());
        Talent talent = Talent.from(request);
        Talent saved = talentRepository.save(talent);

//        // Log activity
//        activityLogger.log(
//            saved.getWorkspaceId(),
//            null,
//            saved.getId(),
//            ActivityType.TALENT_IMPORTED,
//            Map.of(
//                "name", saved.getName(),
//                "sources", getProfileSources(imdbProfile.getData(), spotlightProfile.getData())
//            ),
//            null,
//            userId
//        );

        return saved;
    }

    @Transactional
    public Talent updateTalent(TalentRequest request, String talentId) {
        Talent talent = talentRepository.findById(talentId)
            .orElseThrow(() -> new ResourceNotFoundException("Talent", "id", talentId));

        if (!ObjectUtils.isEmpty(request.getImdbProfileUrl())) {
            talent.setImdbProfileUrl(request.getImdbProfileUrl());
        }
        if (!ObjectUtils.isEmpty(request.getSpotlightProfileUrl())) {
            talent.setSpotlightProfileUrl(request.getSpotlightProfileUrl());
        }
        if (!ObjectUtils.isEmpty(request.getName())) {
            talent.setName(request.getName());
        }
        if (!ObjectUtils.isEmpty(request.getProfilePhotoUrl())) {
            talent.setProfilePhotoUrl(request.getProfilePhotoUrl());
        }
        if (!ObjectUtils.isEmpty(request.getEmail())) {
            talent.setEmail(request.getEmail());
        }
        if (!ObjectUtils.isEmpty(request.getContacts())) {
            talent.setContacts(request.getContacts());
        }

        // Refresh profiles
        TalentProfileMeta imdbProfile = null;
        if (StringUtils.hasText(talent.getImdbProfileUrl())) {
            imdbProfile = profileScraperService.scrapeFromImdb(talent.getImdbProfileUrl());
        }

        TalentProfileMeta spotlightProfile = null;
        if (StringUtils.hasText(talent.getSpotlightProfileUrl())) {
            spotlightProfile = profileScraperService.scrapeFromSpotlight(talent.getSpotlightProfileUrl());
        }

        // Update talent with new profile data
        updateTalentFromProfiles(talent, imdbProfile.getData(), spotlightProfile.getData());
        talent.setProfileMetaId(Optional.ofNullable(imdbProfile.getId()).orElse(null));

        return talentRepository.save(talent);
    }

    private void updateTalentFromProfiles(Talent talent, TalentProfileData imdbProfile, TalentProfileData spotlightProfile) {
        talent.setNameSuffix(imdbProfile != null ? imdbProfile.getNameSuffix() :
            spotlightProfile != null ? spotlightProfile.getNameSuffix() : talent.getNameSuffix());
        talent.setProfilePhotoUrl(imdbProfile != null ? imdbProfile.getPhotoUrl() :
            spotlightProfile != null ? spotlightProfile.getPhotoUrl() : talent.getProfilePhotoUrl());
        talent.setImdbProfileUrl(imdbProfile != null ? imdbProfile.getSourceUrl() : talent.getImdbProfileUrl());
        talent.setSpotlightProfileUrl(spotlightProfile != null ? spotlightProfile.getSourceUrl() : talent.getSpotlightProfileUrl());
        talent.setPersonalDetails(imdbProfile != null ? imdbProfile.getPersonalDetails() : talent.getPersonalDetails());
    }

    private Talent mergeTalentData(TalentRequest request,
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
            .personalDetails(imdbProfile != null ? imdbProfile.getPersonalDetails() : null)
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

        List<ApplicationBriefResponse> applications = applicationRepository
            .findByTalentIdOrderByCreatedAtDesc(id)
            .stream()
            .map(app -> ApplicationBriefResponse.builder()
                .id(app.getId())
                .projectId(app.getProject().getId())
                .projectName(app.getProject().getName())
                .roleName(app.getRole().getName())
                .status(app.getStatus())
                .submittedAt(app.getCreatedAt())
                .build())
            .collect(Collectors.toList());

        List<SubmissionBriefResponse> submissions = submissionRepository
            .findByTalentIdOrderByCreatedAtDesc(id)
            .stream()
            .map(SubmissionBriefResponse::from)
            .collect(Collectors.toList());

        TalentResponse talentResponse = TalentResponse.from(talent);
        talentResponse.setKnownFor(getKnownForMovies(id).stream().map(KnownForItem::from).collect(Collectors.toSet()));
        talentResponse.setSubmissions(submissions);
        talentResponse.setApplications(applications);

        return talentResponse;
    }

    public Page<TalentResponse> searchTalents(String workspaceId, TalentSearchRequest request, Pageable pageable) {
        return talentRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("workspaceId"), workspaceId));

            if (StringUtils.hasText(request.getKeyword())) {
                String keyword = "%" + request.getKeyword().toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), keyword),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), keyword),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), keyword)
                ));
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

    public List<TalentBasicResponse> findAllTalentsBasicInfo() {
        return talentRepository.findAll().stream()
            .map(TalentBasicResponse::from)
            .collect(Collectors.toList());
    }
}