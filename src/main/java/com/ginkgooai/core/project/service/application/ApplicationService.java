package com.ginkgooai.core.project.service.application;

import com.ginkgooai.core.common.exception.ResourceNotFoundException;
import com.ginkgooai.core.project.domain.application.*;
import com.ginkgooai.core.project.domain.project.Project;
import com.ginkgooai.core.project.domain.project.ProjectRole;
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

@Service
@Slf4j
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ProjectRepository projectRepository;
    private final ProjectRoleRepository projectRoleRepository;
    private final TalentRepository talentRepository;
    private final SubmissionRepository submissionRepository;
    private final ApplicationStateMachine stateMachine;

    @Transactional
    public ApplicationResponse createApplication(ApplicationCreateRequest request, String workspaceId, String userId) {
        // First check if talent exists
        Talent talent = talentRepository.findById(request.getTalentId())
                .orElseThrow(() -> new ResourceNotFoundException("Talent", "id", request.getTalentId()));

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
                .agencyName(request.getAgencyName())
                .agentName(request.getAgentName())
                .agentEmail(request.getAgentEmail())
                .status(ApplicationStatus.ADDED)
                .createdBy(userId)
                .build();

        Application savedApplication = applicationRepository.save(application);

        // Deal with optional submissions
        if (!ObjectUtils.isEmpty(request.getSubmissionIds())) {
            List<Submission> submissions = submissionRepository.findAllById(request.getSubmissionIds());
            submissions.forEach(t -> t.setApplication(savedApplication));
            submissionRepository.saveAll(submissions);
        }

        return ApplicationResponse.from(savedApplication);
    }

    @Transactional(readOnly = true)
    public Application getApplicationById(String workspaceId, String id) {
        return findApplicationById(workspaceId, id);
    }

    @Transactional(readOnly = true)
    public Page<ApplicationResponse> listApplications(String workspaceId,
                                                      String projectId,
                                                      String roleId,
                                                      String keyword,
                                                      ApplicationStatus status,
                                                      Pageable pageable) {

        return applicationRepository.findAll(
                buildSpecification(workspaceId, projectId, roleId, keyword, status),
                pageable
        ).map(ApplicationResponse::from);
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
        return applicationRepository.save(application).getComments().stream()
                .map(ApplicationCommentResponse::from)
                .toList();
    }

    @Transactional
    public List<ApplicationNoteResponse> addNote(String workspaceId, String id, String userId, String content) {
        Application application = findApplicationById(workspaceId, id);

        ApplicationNote note = ApplicationNote.builder()
                .application(application)
                .content(content)
                .createdBy(userId)
                .build();

        application.getNotes().add(note);
        return applicationRepository.save(application).getNotes().stream()
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