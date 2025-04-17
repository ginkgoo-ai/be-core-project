package com.ginkgooai.core.project.service.application;

import com.ginkgooai.core.common.enums.ActivityType;
import com.ginkgooai.core.common.exception.ResourceDuplicatedException;
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
import com.ginkgooai.core.project.dto.request.ApplicationStatusUpdateRequest;
import com.ginkgooai.core.project.dto.response.ApplicationCommentResponse;
import com.ginkgooai.core.project.dto.response.ApplicationNoteResponse;
import com.ginkgooai.core.project.dto.response.ApplicationResponse;
import com.ginkgooai.core.project.dto.response.ApplicationStatisticsResponse;
import com.ginkgooai.core.project.repository.*;
import com.ginkgooai.core.project.service.ActivityLoggerService;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
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
    public List<ApplicationResponse> createApplications(ApplicationCreateRequest request,
			String workspaceId, String userId) {
        Project project = projectRepository.findById(request.getProjectId()).orElseThrow(
				() -> new ResourceNotFoundException("Project", "id", request.getProjectId()));

        ProjectRole role = projectRoleRepository.findById(request.getRoleId()).orElseThrow(
				() -> new ResourceNotFoundException("ProjectRole", "id", request.getRoleId()));

		List<Application> existingApplications = applicationRepository.findByRoleId(request.getRoleId());

        Map<String, Application> existingApplicationMap = existingApplications.stream().collect(
				Collectors.toMap(app -> app.getTalent().getId(), Function.identity(), (o, n) -> n));

        List<Application> createdApplications = new ArrayList<>();
        List<String> notFoundTalentIds = new ArrayList<>();
        List<String> alreadyAppliedTalentIds = new ArrayList<>();
        List<Talent> talentsToSave = new ArrayList<>();

        for (String talentId : request.getTalentIds()) {
            if (existingApplicationMap.containsKey(talentId)) {
                alreadyAppliedTalentIds.add(talentId);
                continue;
            }

            Optional<Talent> talentOpt = talentRepository.findById(talentId);
            if (talentOpt.isPresent()) {
                Talent talent = talentOpt.get();
                talent.incrementApplicationCount();
                talentsToSave.add(talent);

				Application application = Application.builder()
					.workspaceId(workspaceId)
					.project(project)
					.role(role)
                        .talent(talent).status(ApplicationStatus.ADDED).build();
                createdApplications.add(application);
            } else {
                notFoundTalentIds.add(talentId);
            }
        }

        if (!alreadyAppliedTalentIds.isEmpty()) {
            String errorDetail = String.format("Role ID: %s, Duplicate talent IDs: %s",
					request.getRoleId(), String.join(",", alreadyAppliedTalentIds));
            throw new ResourceDuplicatedException("Application", "roleId-talentId", errorDetail);
        }

        if (!notFoundTalentIds.isEmpty()) {
            throw new ResourceNotFoundException("Talent", "ids",
					String.join(", ", notFoundTalentIds));
        }

        talentRepository.saveAll(talentsToSave);
        List<Application> savedApplications = applicationRepository.saveAll(createdApplications);

        role.setStatus(RoleStatus.CASTING);
		activityLogger.log(project.getWorkspaceId(), project.getId(), null, // No specific
				ActivityType.ROLE_STATUS_UPDATE,
				Map.of("roleName", role.getName(), "newStatus", role.getStatus().getValue()), null, userId);

		List<CloudFileResponse> videoFiles = Collections.emptyList();
        if (!ObjectUtils.isEmpty(request.getVideoIds())) {
            videoFiles = storageClient.getFileDetails(request.getVideoIds()).getBody();

			role.setStatus(RoleStatus.SUBMITTED);
			activityLogger.log(project.getWorkspaceId(), project.getId(), null, // No
																				// specific
					ActivityType.ROLE_STATUS_UPDATE,
					Map.of("roleName", role.getName(), "newStatus", role.getStatus().getValue()), null, userId);
		}

        final List<CloudFileResponse> finalVideoFiles = videoFiles;
        List<Submission> allSubmissions = new ArrayList<>();

        savedApplications.forEach(savedApplication -> {
            // Create submissions if provided
            if (!finalVideoFiles.isEmpty()) {
                List<Submission> submissions = finalVideoFiles.stream()
					.map(video -> Submission.builder()
						.workspaceId(workspaceId)
						.application(savedApplication)
						.videoName(video.getOriginalName())
						.videoUrl(video.getStoragePath())
						.videoDuration(video.getVideoDuration())
						.videoThumbnailUrl(video.getVideoThumbnailUrl())
						.videoResolution(video.getVideoResolution())
						.mimeType(video.getFileType())
						.build())
					.toList();
                allSubmissions.addAll(submissions);
                savedApplication.setStatus(ApplicationStatus.SUBMITTED);
            }
        });

        if (!allSubmissions.isEmpty()) {
            submissionRepository.saveAll(allSubmissions);
            applicationRepository.saveAll(savedApplications);
        }

        return savedApplications.stream()
			.map(app -> ApplicationResponse.from(app, Collections.emptyList(), userId))
			.collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
	public ApplicationResponse getApplicationById(String applicationId) {
		String workspaceId = ContextUtils.getWorkspaceId();
		String userId = ContextUtils.getUserId();
		Application application = findApplicationById(workspaceId, applicationId);

        List<String> userIds = new ArrayList<>();
        application.getComments().forEach(comment -> userIds.add(comment.getCreatedBy()));
        application.getNotes().forEach(note -> userIds.add(note.getCreatedBy()));
        application.getSubmissions().forEach(submission -> submission.getComments()
			.forEach(comment -> userIds.add(comment.getCreatedBy())));

		// Get role sides files
		Map<String, CloudFileResponse> roleSideFilesMap = Collections.emptyMap();
		if (application.getRole() != null && application.getRole().getSides() != null) {
			try {
				ResponseEntity<List<CloudFileResponse>> response = storageClient
					.getFileDetails(Arrays.asList(application.getRole().getSides()));
				if (response.getBody() != null) {
					roleSideFilesMap = response.getBody()
						.stream()
						.collect(Collectors.toMap(CloudFileResponse::getId, Function.identity()));
				}
			}
			catch (Exception e) {
				log.error("Error fetching side files for role {}: {}", application.getRole().getId(), e.getMessage());
			}
		}

        final List<UserInfoResponse> finalUsers = getUserInfoByIds(userIds);
		final Map<String, CloudFileResponse> finalRoleSideFilesMap = roleSideFilesMap;

		return ApplicationResponse.from(application, finalUsers, userId, finalRoleSideFilesMap);
    }

    @Transactional(readOnly = true)
    public Page<ApplicationResponse> listApplications(String workspaceId, String userId,
			String projectId, String roleId, String talentId, LocalDateTime startDateTime, LocalDateTime endDateTime,
			String viewMode, String keyword, ApplicationStatus status, Pageable pageable) {
		Page<Application> applicationPage = applicationRepository.findAll(
                buildSpecification(workspaceId, projectId, roleId, viewMode, talentId,
						startDateTime, endDateTime, keyword, status, pageable.getSort()),
                pageable);

		// If we're in submissions view mode and have date filters, filter the
		// submissions in memory
        if ("submissions".equals(viewMode) && (startDateTime != null || endDateTime != null)) {
            applicationPage.forEach(app -> {
                if (Objects.nonNull(app.getSubmissions())) {
                    // Filter submissions by date range
					List<Submission> filteredSubmissions = app.getSubmissions().stream().filter(submission -> {
						LocalDateTime createdAt = submission.getCreatedAt();
						if (createdAt == null)
							return false;

						boolean afterStart = startDateTime == null || !createdAt.isBefore(startDateTime);
						boolean beforeEnd = endDateTime == null || !createdAt.isAfter(endDateTime);

						return afterStart && beforeEnd;
					}).collect(Collectors.toList());

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
                        submission.getComments()
							.forEach(comment -> userIds.add(comment.getCreatedBy()));
                    }
                });
            }
        });

        final List<UserInfoResponse> finalUsers = getUserInfoByIds(userIds);

        return applicationPage
			.map(application -> ApplicationResponse.from(application, finalUsers, userId));
    }

    private Specification<Application> buildSpecification(String workspaceId, String projectId,
			String roleId, String viewMode, String talentId, LocalDateTime startDateTime, LocalDateTime endDateTime,
			String keyword, ApplicationStatus status, Sort sort) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            Join<Application, Talent> talentJoin = root.join("talent", JoinType.LEFT);
			Join<Application, ProjectRole> roleJoin = root.join("role", JoinType.LEFT);

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

			// Date filter
			if (startDateTime != null && endDateTime != null) {
				predicates.add(cb.between(root.get("createdAt"), startDateTime, endDateTime));
			}
			else if (startDateTime != null) {
				predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDateTime));
			}
			else if (endDateTime != null) {
				predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endDateTime));
			}

            // Keyword search on talent name or email
            if (StringUtils.hasText(keyword)) {
                String likePattern = "%" + keyword.toLowerCase() + "%";

				Predicate talentNamePredicate = cb.like(
						cb.lower(cb.concat(cb.concat(talentJoin.get("firstName"), " "), talentJoin.get("lastName"))),
						likePattern);
				Predicate talentEmailPredicate = cb.like(cb.lower(talentJoin.get("email")), likePattern);
                Predicate roleNamePredicate = cb.like(cb.lower(roleJoin.get("name")), likePattern);

                predicates.add(cb.or(talentNamePredicate, talentEmailPredicate, roleNamePredicate));
            }

            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                // Add groupBy on the Application ID
                query.groupBy(root.get("id"));

				// Add sort fields to groupBy if needed
				if (sort != null && sort.isSorted()) {
					for (Sort.Order order : sort) {
						if ("talent.firstName".equals(order.getProperty())) {
							query.groupBy(root.get("id"), talentJoin.get("firstName"));
						}
						else if ("role.name".equals(order.getProperty())) {
							query.groupBy(root.get("id"), roleJoin.get("name"));
						}
					}
				}
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Transactional
    public List<ApplicationCommentResponse> addComment(String workspaceId, String id, String userId,
			String content) {
        Application application = findApplicationById(workspaceId, id);

		ApplicationComment comment = ApplicationComment.builder().application(application).content(content).build();

		List<String> userIds = application.getNotes()
			.stream()
			.filter(t -> !ObjectUtils.isEmpty(t.getCreatedBy()))
                .map(ApplicationNote::getCreatedBy).distinct().toList();

		Map<String, UserInfoResponse> userInfoResponses = getUserInfoByIds(userIds).stream()
			.collect(Collectors.toMap(UserInfoResponse::getId, userInfoResponse -> userInfoResponse));

        application.getComments().add(comment);
        application.setStatus(ApplicationStatus.REVIEWED);
        return applicationRepository.save(application).getComments().stream().map(
                t -> ApplicationCommentResponse.from(t, userInfoResponses.get(t.getCreatedBy())))
			.toList();
    }

    @Transactional
    public List<ApplicationNoteResponse> addNote(String workspaceId, String id, String userId,
			String content) {
        Application application = findApplicationById(workspaceId, id);

        ApplicationNote note = applicationNoteRepository
			.save(ApplicationNote.builder().application(application).content(content).build());

        ApplicationNote savedNote = applicationNoteRepository.findById(note.getId()).get();
        application.getNotes().add(savedNote);

		List<String> userIds = application.getNotes()
			.stream()
			.filter(t -> !ObjectUtils.isEmpty(t.getCreatedBy()))
                .map(ApplicationNote::getCreatedBy).distinct().toList();

		Map<String, UserInfoResponse> userInfoResponses = getUserInfoByIds(userIds).stream()
			.collect(Collectors.toMap(UserInfoResponse::getId, userInfoResponse -> userInfoResponse));

        return application.getNotes().stream()
			.map(t -> ApplicationNoteResponse.from(t, userInfoResponses.get(t.getCreatedBy())))
			.toList();
    }

    private Application findApplicationById(String workspaceId, String id) {
        return applicationRepository.findByIdAndWorkspaceId(id, workspaceId)
			.orElseThrow(() -> new ResourceNotFoundException("Application", "workspaceId-applicationId",
					String.join("-", workspaceId, id)));
    }

    private List<UserInfoResponse> getUserInfoByIds(List<String> userIds) {
        List<String> distinctUserIds = userIds.stream().filter(id -> id != null && !id.isEmpty())
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

    public void deleteApplication(String applicationId) {
        Application application = applicationRepository
			.findByIdAndWorkspaceId(applicationId, ContextUtils.getWorkspaceId())
			.orElseThrow(() -> new ResourceNotFoundException("Application", "id", applicationId));

        if (application.getStatus() != ApplicationStatus.ADDED) {
            throw new IllegalStateException(
					"Cannot delete application with status: " + application.getStatus());
        }

    }

    /**
	 * Get application status counts for a specific project
	 * @param projectId The project ID
	 * @param roleId Optional role ID to filter applications
	 * @return ApplicationStatusCountResponse with counts by status
	 */
    public ApplicationStatisticsResponse getApplicationStatusCountsByProject(String projectId, String roleId) {
        String workspaceId = ContextUtils.getWorkspaceId();
        // Verify project exists and belongs to the workspace
        projectRepository.findByIdAndWorkspaceId(projectId, workspaceId)
			.orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        List<Object[]> results;
        if (StringUtils.hasText(roleId)) {
            projectRoleRepository.findById(roleId)
				.orElseThrow(() -> new ResourceNotFoundException("ProjectRole", "id", roleId));
			results = applicationRepository.countByProjectIdAndRoleIdGroupByStatus(projectId, roleId);
        } else {
            results = applicationRepository.countByProjectIdGroupByStatus(projectId);
        }

        Map<ApplicationStatus, Long> statusCounts = convertToStatusCountMap(results);

        return ApplicationStatisticsResponse.from(statusCounts);
    }

    /**
     * Convert query results to a map of status to count
     *
     * @param results List of Object[] containing status and count
     * @return Map of ApplicationStatus to count
     */
    private Map<ApplicationStatus, Long> convertToStatusCountMap(List<Object[]> results) {
        Map<ApplicationStatus, Long> statusCounts = new HashMap<>();

        for (Object[] result : results) {
            ApplicationStatus status = (ApplicationStatus) result[0];
            Long count = ((Number) result[1]).longValue();
            statusCounts.put(status, count);
        }

        return statusCounts;
    }

    /**
	 * Update the status of an application
	 * @param applicationId ID of the application to update
	 * @param workspaceId Workspace ID for security check
	 * @param request Status update request containing new status and optional comment
	 * @return Updated application response
	 */
    @Transactional
    public ApplicationResponse updateApplicationStatus(String applicationId, String workspaceId,
			ApplicationStatusUpdateRequest request) {
        // Find application and verify it belongs to the workspace
        Application application = findApplicationById(workspaceId, applicationId);

        // Get previous status for activity logging
        ApplicationStatus previousStatus = application.getStatus();

        // Update the status
        application.setStatus(request.getStatus());

        // Save the updated application
        application = applicationRepository.save(application);

        // Log the activity to-do

        // Return updated application
        return ApplicationResponse.from(application);
    }
}
