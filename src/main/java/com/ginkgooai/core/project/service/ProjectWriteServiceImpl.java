package com.ginkgooai.core.project.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ginkgooai.core.common.bean.ActivityType;
import com.ginkgooai.core.common.exception.ResourceNotFoundException;
import com.ginkgooai.core.project.client.storage.StorageClient;
import com.ginkgooai.core.project.client.storage.dto.CloudFileResponse;
import com.ginkgooai.core.project.domain.application.Application;
import com.ginkgooai.core.project.domain.project.Project;
import com.ginkgooai.core.project.domain.project.ProjectStatus;
import com.ginkgooai.core.project.domain.role.ProjectRole;
import com.ginkgooai.core.project.dto.request.ProjectCreateRequest;
import com.ginkgooai.core.project.dto.request.ProjectResponse;
import com.ginkgooai.core.project.dto.request.ProjectRolePatchRequest;
import com.ginkgooai.core.project.dto.request.ProjectRoleRequest;
import com.ginkgooai.core.project.dto.request.ProjectUpdateRequest;
import com.ginkgooai.core.project.repository.ApplicationRepository;
import com.ginkgooai.core.project.repository.ProjectRepository;
import com.ginkgooai.core.project.repository.ProjectRoleRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ProjectWriteServiceImpl implements ProjectWriteService {

    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private ProjectRoleRepository projectRoleRepository;
    @Autowired
    private ActivityLoggerService activityLogger;
    @Autowired
    private StorageClient storageClient;
    @Autowired
    private ApplicationRepository applicationRepository;

    @Override
    @Transactional
    public ProjectResponse createProject(ProjectCreateRequest request, String workspaceId, String userId) {
        log.debug("Creating new project with request: {}", request);

        validateProjectRequest(request);

        Project project = new Project(request, workspaceId, userId);
        Project savedProject = projectRepository.save(project);

        // Set optional status if provided (override default DRAFTING if necessary)
        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
        }

        // Initialize roles and bind to project
        List<CloudFileResponse> roleSideFiles = new ArrayList<>();
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            request.getRoles().forEach(roleRequest -> {
                ProjectRole role = ProjectRole.builder()
                        .name(roleRequest.getName())
                        .characterDescription(roleRequest.getCharacterDescription())
                        .selfTapeInstructions(roleRequest.getSelfTapeInstructions())
                        .isActive(roleRequest.getIsActive() != null ? roleRequest.getIsActive() : true)
                        .sides(roleRequest.getSides().toArray(new String[0]))
                        .project(savedProject)
                        .build();
                projectRoleRepository.save(role);

                activityLogger.log(
                        workspaceId,
                        savedProject.getId(),
                        null,
                        ActivityType.ROLE_CREATED,
                        Map.of(
                                "user", userId,
                                "projectName", savedProject.getName(),
                                "roleName", role.getName()),
                        null,
                        userId);
            });

            roleSideFiles = storageClient
                    .getFileDetails(request.getRoles().stream().flatMap(roles -> roles.getSides().stream()).toList())
                    .getBody();
            log.debug("Roles sides files: {}", roleSideFiles);
        }

        activityLogger.log(
                workspaceId,
                savedProject.getId(),
                null,
                ActivityType.PROJECT_CREATED,
                Map.of(
                        "user", userId,
                        "projectName", savedProject.getName()),
                null,
                userId);

        return ProjectResponse.from(savedProject, roleSideFiles);
    }

    private void validateProjectRequest(ProjectCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Project request cannot be null");
        }
        if (StringUtils.isBlank(request.getName())) {
            throw new IllegalArgumentException("Project name cannot be empty");
        }
    }

    @Override
    @Transactional
    public Project updateProject(String projectId, ProjectUpdateRequest request, String workspaceId) {
        Project project = projectRepository.findByIdAndWorkspaceId(projectId, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id&workspaceId",
                        projectId + ":" + workspaceId));

        project.updateDetails(request.getName(), request.getDescription(), request.getPlotLine(), request.getStatus(),
                request.getPosterUrl());

        Project updatedProject = projectRepository.save(project);

        return updatedProject;
    }

    @Override
    @Transactional
    public Project updateProjectStatus(String id, ProjectStatus status) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));

        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }

        if (status == project.getStatus()) {
            return project;
        }

        project.setStatus(status);
        project.setLastActivityAt(LocalDateTime.now());

        Project updatedProject = projectRepository.save(project);
        activityLogger.log(
                project.getWorkspaceId(),
                project.getId(),
                null,
                ActivityType.PROJECT_STATUS_CHANGE,
                Map.of(
                        "project", updatedProject.getName(),
                        "previousStatus", project.getStatus().getValue(),
                        "newStatus", status,
                        "newStatus", updatedProject.getStatus().getValue()),
                null,
                updatedProject.getCreatedBy());

        return updatedProject;
    }

    @Override
    @Transactional
    public void deleteProject(String id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        projectRepository.delete(project);
    }

    @Override
    @Transactional
    public ProjectRole createRole(String projectId, ProjectRoleRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "projectId", projectId));

        ProjectRole role = new ProjectRole();
        role.setName(request.getName());
        role.setCharacterDescription(request.getCharacterDescription());
        role.setSelfTapeInstructions(request.getSelfTapeInstructions());
        role.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        role.setProject(project);

        ProjectRole savedRole = projectRoleRepository.save(role);
        project.addRole(savedRole); // Update the project aggregate
        projectRepository.save(project); // Save to persist the relationship

        return savedRole;
    }

    @Override
    @Transactional
    public ProjectRole updateRole(String roleId, ProjectRoleRequest request) {
        ProjectRole role = projectRoleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "roleId", roleId));

        role.setName(request.getName());
        role.setCharacterDescription(request.getCharacterDescription());
        role.setSelfTapeInstructions(request.getSelfTapeInstructions());
        if (request.getIsActive() != null) {
            role.setIsActive(request.getIsActive());
        }

        ProjectRole savedRole = projectRoleRepository.save(role);

        return savedRole;
    }

    @Override
    public ProjectRole patchRoleDetails(String roleId, ProjectRolePatchRequest request) {
        ProjectRole role = projectRoleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "roleId", roleId));

        // Only update fields that are provided in the request
        if (request.getCharacterDescription() != null) {
            role.setCharacterDescription(request.getCharacterDescription());
        }
        if (request.getSelfTapeInstructions() != null) {
            role.setSelfTapeInstructions(request.getSelfTapeInstructions());
        }
        if (request.getSides() != null) {
            role.setSides(request.getSides().toArray(new String[0]));
        }
        return projectRoleRepository.save(role);
    }

    @Override
    @Transactional
    public void deleteRole(String roleId) {
        ProjectRole role = projectRoleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "roleId", roleId));

        List<Application> dependentApplications = applicationRepository.findByRoleId(roleId);
        if (!dependentApplications.isEmpty()) {
            throw new IllegalArgumentException("Role '" + roleId + "' already has applications");
        }

        Project project = role.getProject();
        project.removeRole(roleId);

        projectRepository.save(project);
        projectRoleRepository.delete(role);
    }

}