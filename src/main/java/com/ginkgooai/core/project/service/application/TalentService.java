package com.ginkgooai.core.project.service.application;

import com.ginkgooai.core.common.enums.ActivityType;
import com.ginkgooai.core.common.exception.ResourceNotFoundException;
import com.ginkgooai.core.common.utils.ContextUtils;
import com.ginkgooai.core.project.domain.application.Application;
import com.ginkgooai.core.project.domain.application.ApplicationStatus;
import com.ginkgooai.core.project.domain.talent.ImdbMovieItem;
import com.ginkgooai.core.project.domain.talent.Talent;
import com.ginkgooai.core.project.domain.talent.TalentProfileMeta;
import com.ginkgooai.core.project.domain.talent.TalentStatus;
import com.ginkgooai.core.project.dto.KnownForItem;
import com.ginkgooai.core.project.dto.TalentProfileData;
import com.ginkgooai.core.project.dto.request.TalentRequest;
import com.ginkgooai.core.project.dto.request.TalentSearchRequest;
import com.ginkgooai.core.project.dto.response.*;
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
import java.util.function.Function;
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
//        TalentProfileMeta imdbProfile = null;
//        if (StringUtils.hasText(request.getImdbProfileUrl())) {
//            imdbProfile = profileScraperService.scrapeFromImdb(request.getImdbProfileUrl());
//        }
//
//        TalentProfileMeta spotlightProfile = null;
//        if (StringUtils.hasText(request.getSpotlightProfileUrl())) {
//            spotlightProfile = profileScraperService.scrapeFromSpotlight(request.getSpotlightProfileUrl());
//        }

        // Merge profiles with manual input
//        Talent talent = mergeTalentData(request, imdbProfile.getData(), spotlightProfile.getData());
        Talent talent = Talent.from(request);
        Talent saved = talentRepository.save(talent);

        // Log activity
        activityLogger.log(
            saved.getWorkspaceId(),
            null,
            saved.getId(),
				ActivityType.TALENT_IMPORTED,
            Map.of(
						"talentName",
						String.join(" ", talent.getFirstName(), talent.getLastName())
            ),
            null,
            ContextUtils.getUserId()
        );

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
        if (!ObjectUtils.isEmpty(request.getFirstName())) {
            talent.setFirstName(request.getFirstName());
        }
        if (!ObjectUtils.isEmpty(request.getLastName())) {
            talent.setLastName(request.getLastName());
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
//        if (StringUtils.hasText(talent.getImdbProfileUrl())) {
//            imdbProfile = profileScraperService.scrapeFromImdb(talent.getImdbProfileUrl());
//        }
//
//        TalentProfileMeta spotlightProfile = null;
//        if (StringUtils.hasText(talent.getSpotlightProfileUrl())) {
//            spotlightProfile = profileScraperService.scrapeFromSpotlight(talent.getSpotlightProfileUrl());
//        }

        // Update talent with new profile data
//        updateTalentFromProfiles(talent, imdbProfile.getData(), spotlightProfile.getData());
//        talent.setProfileMetaId(Optional.ofNullable(imdbProfile.getId()).orElse(null));

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
        return talentRepository.findByWorkspaceId(ContextUtils.getWorkspaceId()).stream()
            .map(TalentBasicResponse::from)
            .collect(Collectors.toList());
    }


    /**
     * Get all talents with their application status for a specific role
     *
     * @param workspaceId The workspace ID
     * @param roleId The role ID to check application status
     * @param name Optional name parameter for fuzzy matching
     * @return List of talents with application status
     */
    public List<TalentWithApplicationStatusResponse> getAllTalentsWithApplicationStatus(
        String workspaceId, String roleId, String name) {
        // Get talents in the workspace with optional name filter
        List<Talent> talents;
        if (StringUtils.hasText(name)) {
            // Use fuzzy matching for name
            talents = talentRepository.findByWorkspaceIdAndNameMatching(workspaceId, name);
        } else {
            talents = talentRepository.findByWorkspaceId(workspaceId);
        }

        // Get all applications for the role
        List<Application> applications = applicationRepository.findByRoleId(roleId);

        // Create a map of talent ID to application for quick lookup
        Map<String, Application> applicationMap = applications.stream()
            .collect(Collectors.toMap(
                app -> app.getTalent().getId(),
                Function.identity(),
                (existing, replacement) -> existing
            ));

        // Map talents to response objects with application status
        return talents.stream()
            .map(talent -> {
                Application application = applicationMap.get(talent.getId());
                boolean hasApplied = application != null;
                ApplicationStatus status = hasApplied ? application.getStatus() : null;
                String applicationId = hasApplied ? application.getId() : null;

                return TalentWithApplicationStatusResponse.from(
                    talent,
                    status,
                    hasApplied,
                    applicationId
                );
            })
            .collect(Collectors.toList());
    }

}