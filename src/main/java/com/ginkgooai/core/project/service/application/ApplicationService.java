package com.ginkgooai.core.project.service.application;

import com.ginkgooai.core.common.enums.ActivityType;
import com.ginkgooai.core.common.exception.ResourceNotFoundException;
import com.ginkgooai.core.common.utils.ContextUtils;
import com.ginkgooai.core.project.client.identity.IdentityClient;
import com.ginkgooai.core.project.client.identity.dto.UserInfoResponse;
import com.ginkgooai.core.project.client.storage.StorageClient;
import com.ginkgooai.core.project.client.storage.dto.CloudFileResponse;
import com.ginkgooai.core.project.domain.application.*;
import com.ginkgooai.core.project.domain.project.Project;
import com.ginkgooai.core.project.domain.role.ProjectRole;
import com.ginkgooai.core.project.domain.role.RoleStatus;
import com.ginkgooai.core.project.domain.talent.Talent;
import com.ginkgooai.core.project.dto.request.ApplicationCreateRequest;
import com.ginkgooai.core.project.dto.response.ApplicationCommentResponse;
import com.ginkgooai.core.project.dto.response.ApplicationNoteResponse;
import com.ginkgooai.core.project.dto.response.ApplicationResponse;
import com.ginkgooai.core.project.repository.*;
import com.ginkgooai.core.project.service.ActivityLoggerService;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
            talent = talentService.createTalentFromProfiles(request.getTalent());

            activityLogger.log(
                    workspaceId,
                    project.getId(),
                    null,
                    ActivityType.TALENT_ADDED,
                    Map.of(
                        "talentName", String.join(" ", talent.getFirstName(), talent.getEmail()),
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
                                                      String talentId,
                                                      LocalDateTime startDateTime,
                                                      LocalDateTime endDateTime,
                                                      String viewMode,
                                                      String keyword,
                                                      ApplicationStatus status,
                                                      Pageable pageable) {

        Page<Application> applicationPage = applicationRepository.findAll(
                buildSpecification(workspaceId, projectId, roleId, viewMode, talentId, startDateTime, endDateTime, keyword, status),
                pageable);

        // If we're in submissions view mode and have date filters, filter the submissions in memory
        if ("submissions".equals(viewMode) && (startDateTime != null || endDateTime != null)) {
            applicationPage.forEach(app -> {
                if (Objects.nonNull(app.getSubmissions())) {
                    // Filter submissions by date range
                    List<Submission> filteredSubmissions = app.getSubmissions().stream()
                            .filter(submission -> {
                                LocalDateTime createdAt = submission.getCreatedAt();
                                if (createdAt == null) return false;

                                boolean afterStart = startDateTime == null || !createdAt.isBefore(startDateTime);
                                boolean beforeEnd = endDateTime == null || !createdAt.isAfter(endDateTime);

                                return afterStart && beforeEnd;
                            })
                            .collect(Collectors.toList());

                    // Replace the submissions list with the filtered one
                    app.setSubmissions(filteredSubmissions);
                }
            });
        }
        
        List<String> userIds = new ArrayList<>();
        applicationPage.forEach(app -> {
            if (Objects.nonNull(app.getComments())) {
                app.getComments().forEach(comment -> userIds.add(comment.getCreatedBy()));
            }
            if (Objects.nonNull(app.getNotes())) {
                app.getNotes().forEach(note -> userIds.add(note.getCreatedBy()));
            }
            if (Objects.nonNull(app.getSubmissions())) {
                app.getSubmissions().forEach(submission -> {
                            if (Objects.nonNull(submission.getComments())) {
                                submission.getComments().forEach(comment -> userIds.add(comment.getCreatedBy()));
                            }
                        });
            }
        });

        final List<UserInfoResponse> finalUsers = getUserInfoByIds(userIds);

        return applicationPage.map(application -> ApplicationResponse.from(application, finalUsers, userId));
    }

    private Specification<Application> buildSpecification(String workspaceId,
                                                          String projectId,
                                                          String roleId,
                                                          String viewMode,
                                                          String talentId,
                                                          LocalDateTime startDateTime,
                                                          LocalDateTime endDateTime,
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

            // Talent filter
            if (StringUtils.hasText(talentId)) {
                predicates.add(cb.equal(root.get("talent").get("id"), talentId));
            }

      

            // Status filter
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            // viewMode filter
            if ("submissions".equals(viewMode)) {
                Join<Application, Submission> submissionJoin = root.join("submissions", JoinType.LEFT);
                predicates.add(cb.isNotNull(submissionJoin.get("id")));
                query.distinct(true);

                // Date filter
                if (startDateTime != null && endDateTime != null) {
                    predicates.add(cb.between(submissionJoin.get("createdAt"), startDateTime, endDateTime));
                } else if (startDateTime != null) {
                    predicates.add(cb.greaterThanOrEqualTo(submissionJoin.get("createdAt"), startDateTime));
                } else if (endDateTime != null) {
                    predicates.add(cb.lessThanOrEqualTo(submissionJoin.get("createdAt"), endDateTime));
                }
            } else {
                if (startDateTime != null && endDateTime != null) {
                    predicates.add(cb.between(root.get("createdAt"), startDateTime, endDateTime));
                } else if (startDateTime != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDateTime));
                } else if (endDateTime != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endDateTime));
                }
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
                .build();

        List<String> userIds = application.getNotes().stream()
                .filter(t -> !ObjectUtils.isEmpty(t.getCreatedBy()))
                .map(ApplicationNote::getCreatedBy).distinct().toList();

        Map<String, UserInfoResponse> userInfoResponses = getUserInfoByIds(userIds).stream().collect(
                Collectors.toMap(UserInfoResponse::getId, userInfoResponse -> userInfoResponse));

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
                .build());

        ApplicationNote savedNote = applicationNoteRepository.findById(note.getId()).get();
        application.getNotes().add(savedNote);

        List<String> userIds = application.getNotes().stream()
                .filter(t -> !ObjectUtils.isEmpty(t.getCreatedBy()))
                .map(ApplicationNote::getCreatedBy).distinct().toList();

        Map<String, UserInfoResponse> userInfoResponses = getUserInfoByIds(userIds).stream().collect(
                Collectors.toMap(UserInfoResponse::getId, userInfoResponse -> userInfoResponse));

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