package com.ginkgooai.core.project.service.application;

import com.ginkgooai.core.common.exception.ResourceNotFoundException;
import com.ginkgooai.core.project.client.storage.StorageClient;
import com.ginkgooai.core.project.client.storage.dto.CloudFileResponse;
import com.ginkgooai.core.project.domain.application.*;
import com.ginkgooai.core.project.domain.project.Project;
import com.ginkgooai.core.project.domain.role.ProjectRole;
import com.ginkgooai.core.project.domain.talent.Talent;
import com.ginkgooai.core.project.dto.request.ApplicationCreateRequest;
import com.ginkgooai.core.project.dto.response.ApplicationCommentResponse;
import com.ginkgooai.core.project.dto.response.ApplicationNoteResponse;
import com.ginkgooai.core.project.dto.response.ApplicationResponse;
import com.ginkgooai.core.project.repository.*;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    private final ApplicationStateMachine stateMachine;
    private final TalentService talentService;
    private final StorageClient storageClient;

    @Transactional
    public ApplicationResponse createApplication(ApplicationCreateRequest request, String workspaceId, String userId) {
        // Create the talent if not exits
        Talent talent;
        if (!ObjectUtils.isEmpty(request.getTalentId())) {
            talent = talentRepository.findById(request.getTalentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Talent", "id", request.getTalentId()));
        } else if (Objects.nonNull(request.getTalent())) 
            talent = talentService.createTalentFromProfiles(request.getTalent(), workspaceId, userId);
        else {
            throw new IllegalArgumentException("Talent ID or Talent object must be provided");
        }

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", request.getProjectId()));

        ProjectRole role = projectRoleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("ProjectRole", "id", request.getRoleId()));

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

        // Create submissions if provided
        log.debug("Video files: {}", request.getVideoIds());
        if (!ObjectUtils.isEmpty(request.getVideoIds())) {
            List<CloudFileResponse> videoFiles = storageClient.getFileDetails(request.getVideoIds()).getBody();
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

        return ApplicationResponse.from(savedApplication, userId);
    }

    @Transactional(readOnly = true)
    public Application getApplicationById(String workspaceId, String id) {
        return findApplicationById(workspaceId, id);
    }

    @Transactional(readOnly = true)
    public Page<ApplicationResponse> listApplications(String workspaceId,
                                                      String userId,
                                                      String projectId,
                                                      String roleId,
                                                      String keyword,
                                                      ApplicationStatus status,
                                                      Pageable pageable) {

        return applicationRepository.findAll(
                buildSpecification(workspaceId, projectId, roleId, keyword, status),
                pageable
        ).map(application -> ApplicationResponse.from(application, userId));
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
                Predicate talentEmailPredicate = cb.like(cb.lower(talentJoin.get("email")), likePattern);
                Predicate agentNamePredicate = cb.like(cb.lower(root.get("agentName")), likePattern);
                Predicate agentEmailPredicate = cb.like(cb.lower(root.get("agentEmail")), likePattern);
                Predicate roleNamePredicate = cb.like(cb.lower(roleJoin.get("name")), likePattern);

                predicates.add(cb.or(
                        talentNamePredicate,
                        talentEmailPredicate,
                        agentNamePredicate,
                        agentEmailPredicate,
                        roleNamePredicate
                ));
            }

            // Make query distinct to avoid duplicates
            query.distinct(true);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Transactional
    public List<ApplicationCommentResponse> addComment(String workspaceId, String id, String userId, String content) {
        Application application = findApplicationById(workspaceId, id);

        ApplicationComment comment = ApplicationComment.builder()
                .application(application)
                .content(content)
                .createdBy(userId)
                .build();

        application.getComments().add(comment);
        application.setStatus(ApplicationStatus.REVIEWED);
        return applicationRepository.save(application).getComments().stream()
                .map(ApplicationCommentResponse::from)
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

        List<String> userIds = application.getNotes().stream().filter(t -> !ObjectUtils.isEmpty(t.getCreatedBy())).map(ApplicationNote::getCreatedBy).distinct().toList();
        
        return application.getNotes().stream()
                .map(ApplicationNoteResponse::from)
                .toList();
    }

    private Application findApplicationById(String workspaceId, String id) {
        return applicationRepository.findOne(
                (root, query, cb) -> cb.and(
                        cb.equal(root.get("id"), id),
                        cb.equal(root.get("workspaceId"), workspaceId)
                )
        ).orElseThrow(() -> new ResourceNotFoundException("Application", "id", id));
    }

}