package com.ginkgooai.core.project.service.application;

import java.util.*;
import java.util.stream.Collectors;

import com.ginkgooai.core.common.utils.ContextUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.ginkgooai.core.common.bean.ActivityType;
import com.ginkgooai.core.common.exception.ResourceNotFoundException;
import com.ginkgooai.core.project.client.identity.IdentityClient;
import com.ginkgooai.core.project.client.identity.dto.UserInfoResponse;
import com.ginkgooai.core.project.client.storage.StorageClient;
import com.ginkgooai.core.project.client.storage.dto.CloudFileResponse;
import com.ginkgooai.core.project.domain.application.Application;
import com.ginkgooai.core.project.domain.application.ApplicationComment;
import com.ginkgooai.core.project.domain.application.ApplicationNote;
import com.ginkgooai.core.project.domain.application.ApplicationStatus;
import com.ginkgooai.core.project.domain.application.Submission;
import com.ginkgooai.core.project.domain.project.Project;
import com.ginkgooai.core.project.domain.role.ProjectRole;
import com.ginkgooai.core.project.domain.role.RoleStatus;
import com.ginkgooai.core.project.domain.talent.Talent;
import com.ginkgooai.core.project.dto.request.ApplicationCreateRequest;
import com.ginkgooai.core.project.dto.response.ApplicationCommentResponse;
import com.ginkgooai.core.project.dto.response.ApplicationNoteResponse;
import com.ginkgooai.core.project.dto.response.ApplicationResponse;
import com.ginkgooai.core.project.repository.ApplicationNoteRepository;
import com.ginkgooai.core.project.repository.ApplicationRepository;
import com.ginkgooai.core.project.repository.ProjectRepository;
import com.ginkgooai.core.project.repository.ProjectRoleRepository;
import com.ginkgooai.core.project.repository.SubmissionRepository;
import com.ginkgooai.core.project.repository.TalentRepository;
import com.ginkgooai.core.project.service.ActivityLoggerService;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationNoteRepository applicationNoteRepository;
    private final ProjectRepository projectRepository;
    private final ProjectRoleRepository projectRoleRepository;
    private final TalentRepository talentRepository;
    private final SubmissionRepository submissionRepository;
    private final TalentService talentService;
    private final StorageClient storageClient;
    private final IdentityClient identityClient;
    private final ActivityLoggerService activityLogger;

    @Transactional
    public ApplicationResponse createApplication(ApplicationCreateRequest request, String workspaceId,
                                                 String userId) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id",
                        request.getProjectId()));

        ProjectRole role = projectRoleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("ProjectRole", "id",
                        request.getRoleId()));

        // Create the talent if not exits
        Talent talent;
        if (!ObjectUtils.isEmpty(request.getTalentId())) {
            talent = talentRepository.findById(request.getTalentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Talent", "id",
                            request.getTalentId()));
        } else if (Objects.nonNull(request.getTalent())) {
            talent = talentService.createTalentFromProfiles(request.getTalent(), workspaceId, userId);

            activityLogger.log(
                    workspaceId,
                    project.getId(),
                    null,
                    ActivityType.TALENT_ADDED,
                    Map.of(
                            "talentName", talent.getName(),
                            "user", userId),
                    null,
                    userId);

        } else {
            throw new IllegalArgumentException("Talent ID or Talent object must be provided");
        }

        role.setStatus(RoleStatus.CASTING);

        // Create the application
        Application application = Application.builder()
                .workspaceId(workspaceId)
                .project(project)
                .role(role)
                .talent(talent)
                .status(ApplicationStatus.ADDED)
                .createdBy(userId)
                .build();

        Application savedApplication = applicationRepository.save(application);

        // Log activity
        activityLogger.log(
                project.getWorkspaceId(),
                project.getId(),
                savedApplication.getId(),
                ActivityType.ROLE_STATUS_UPDATE,
                Map.of(
                        "roleName", role.getName(),
                        "newStatus", role.getStatus().getValue()),
                null,
                userId);

        // Create submissions if provided
        log.debug("Video files: {}", request.getVideoIds());
        if (!ObjectUtils.isEmpty(request.getVideoIds())) {
            List<CloudFileResponse> videoFiles = storageClient.getFileDetails(request.getVideoIds())
                    .getBody();
            log.debug("Video files: {}", videoFiles);
            List<Submission> submissions = videoFiles.stream().map(video -> Submission.builder()
                    .workspaceId(workspaceId)
                    .application(savedApplication)
                    .videoName(video.getOriginalName())
                    .videoUrl(video.getStoragePath())
                    .videoDuration(video.getVideoDuration())
                    .videoThumbnailUrl(video.getVideoThumbnailUrl())
                    .videoResolution(video.getVideoResolution())
                    .mimeType(video.getFileType())
                    .createdBy(userId)
                    .build()).toList();
            List<Submission> savedSubmissions = submissionRepository.saveAll(submissions);

            savedApplication.setSubmissions(savedSubmissions);
            savedApplication.setStatus(ApplicationStatus.SUBMITTED);
        }

        return ApplicationResponse.from(savedApplication, Collections.emptyList(), userId);
    }

    @Transactional(readOnly = true)
    public ApplicationResponse getApplicationById(String workspaceId, String id) {
        Application application = findApplicationById(workspaceId, id);

        List<String> userIds = new ArrayList<>();
        application.getComments().forEach(comment -> userIds.add(comment.getCreatedBy()));
        application.getNotes().forEach(note -> userIds.add(note.getCreatedBy()));
        application.getSubmissions().forEach(submission -> submission.getComments()
                    .forEach(comment -> userIds.add(comment.getCreatedBy())));

        final List<UserInfoResponse> finalUsers = getUserInfoByIds(userIds);
        
        return ApplicationResponse.from(application, finalUsers, ContextUtils.getUserId());
    }

    @Transactional(readOnly = true)
    public Page<ApplicationResponse> listApplications(String workspaceId,
                                                      String userId,
                                                      String projectId,
                                                      String roleId,
                                                      String keyword,
                                                      ApplicationStatus status,
                                                      Pageable pageable) {

        Page<Application> applicationPage = applicationRepository.findAll(
                buildSpecification(workspaceId, projectId, roleId, keyword, status),
                pageable);

        List<String> userIds = new ArrayList<>();
        applicationPage.forEach(app -> {
            CollectionUtils.emptyIfNull(app.getComments()).forEach(comment -> userIds.add(comment.getCreatedBy()));
            CollectionUtils.emptyIfNull(app.getNotes()).forEach(note -> userIds.add(note.getCreatedBy()));
            CollectionUtils.emptyIfNull(app.getSubmissions()).forEach(submission -> CollectionUtils.emptyIfNull(submission.getComments())
                    .forEach(comment -> userIds.add(comment.getCreatedBy())));
        });

        final List<UserInfoResponse> finalUsers = getUserInfoByIds(userIds);
        
        return applicationPage.map(application -> ApplicationResponse.from(application, finalUsers, userId));
    }

    private Specification<Application> buildSpecification(String workspaceId,
                                                          String projectId,
                                                          String roleId,
                                                          String keyword,
                                                          ApplicationStatus status) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Workspace filter (required)
            predicates.add(cb.equal(root.get("workspaceId"), workspaceId));

            // Project filter
            if (StringUtils.hasText(projectId)) {
                predicates.add(cb.equal(root.get("project").get("id"), projectId));
            }

            // Role filter
            if (StringUtils.hasText(roleId)) {
                predicates.add(cb.equal(root.get("role").get("id"), roleId));
            }

            // Status filter
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            // Keyword search on talent name or email
            if (StringUtils.hasText(keyword)) {
                String likePattern = "%" + keyword.toLowerCase() + "%";

                Join<Application, Talent> talentJoin = root.join("talent", JoinType.LEFT);
                Join<Application, ProjectRole> roleJoin = root.join("role", JoinType.LEFT);

                Predicate talentNamePredicate = cb.like(cb.lower(talentJoin.get("name")), likePattern);
                Predicate talentEmailPredicate = cb.like(cb.lower(talentJoin.get("email")),
                        likePattern);
                // Predicate agentNamePredicate = cb.like(cb.lower(root.get("agentName")),
                // likePattern);
                Predicate agentEmailPredicate = cb.like(cb.lower(talentJoin.get("agentEmail")),
                        likePattern);
                Predicate roleNamePredicate = cb.like(cb.lower(roleJoin.get("name")), likePattern);

                predicates.add(cb.or(
                        talentNamePredicate,
                        talentEmailPredicate,
                        // agentNamePredicate,
                        agentEmailPredicate,
                        roleNamePredicate));
            }

            // Make query distinct to avoid duplicates
            query.distinct(true);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Transactional
    public List<ApplicationCommentResponse> addComment(String workspaceId, String id, String userId,
                                                       String content) {
        Application application = findApplicationById(workspaceId, id);

        ApplicationComment comment = ApplicationComment.builder()
                .application(application)
                .content(content)
                .createdBy(userId)
                .build();

        List<String> userIds = application.getNotes().stream()
                .filter(t -> !ObjectUtils.isEmpty(t.getCreatedBy()))
                .map(ApplicationNote::getCreatedBy).distinct().toList();
        
        Map<String, UserInfoResponse> userInfoResponses = getUserInfoByIds(userIds).stream().collect(Collectors.toMap(UserInfoResponse::getId, userInfoResponse -> userInfoResponse));

        application.getComments().add(comment);
        application.setStatus(ApplicationStatus.REVIEWED);
        return applicationRepository.save(application).getComments().stream()
                .map(t -> ApplicationCommentResponse.from(t, userInfoResponses.get(t.getCreatedBy())))
                .toList();
    }

    @Transactional
    public List<ApplicationNoteResponse> addNote(String workspaceId, String id, String userId, String content) {
        Application application = findApplicationById(workspaceId, id);

        ApplicationNote note = applicationNoteRepository.save(ApplicationNote.builder()
                .application(application)
                .content(content)
                .createdBy(userId)
                .build());

        ApplicationNote savedNote = applicationNoteRepository.findById(note.getId()).get();
        application.getNotes().add(savedNote);


        List<String> userIds = application.getNotes().stream()
                .filter(t -> !ObjectUtils.isEmpty(t.getCreatedBy()))
                .map(ApplicationNote::getCreatedBy).distinct().toList();

        Map<String, UserInfoResponse> userInfoResponses = getUserInfoByIds(userIds).stream().collect(Collectors.toMap(UserInfoResponse::getId, userInfoResponse -> userInfoResponse));

        return application.getNotes().stream()
                .map(t -> ApplicationNoteResponse.from(t, userInfoResponses.get(t.getCreatedBy())))
                .toList();
    }

    private Application findApplicationById(String workspaceId, String id) {
        return applicationRepository.findOne(
                        (root, query, cb) -> cb.and(
                                cb.equal(root.get("id"), id),
                                cb.equal(root.get("workspaceId"), workspaceId)))
                .orElseThrow(() -> new ResourceNotFoundException("Application", "id", id));
    }
    
    private List<UserInfoResponse> getUserInfoByIds(List<String> userIds) {
        List<String> distinctUserIds = userIds.stream()
                .filter(id -> id != null && !id.isEmpty())
                .distinct()
                .collect(Collectors.toList());

        List<UserInfoResponse> users = new ArrayList<>();
        if (!distinctUserIds.isEmpty()) {
            try {
                users = identityClient.getUsersByIds(distinctUserIds).getBody();
                if (users == null) {
                    users = new ArrayList<>();
                    log.warn("Failed to get user information from identity service");
                }
            } catch (Exception e) {
                log.error("Error fetching user information: {}", e.getMessage());
            }
        }
        
        return users;
    }

}