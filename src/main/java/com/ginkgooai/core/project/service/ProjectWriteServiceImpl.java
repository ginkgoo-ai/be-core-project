package com.ginkgooai.core.project.service;

import com.ginkgooai.core.common.enums.ActivityType;
import com.ginkgooai.core.common.exception.ConflictException;
import com.ginkgooai.core.common.exception.ResourceNotFoundException;
import com.ginkgooai.core.common.utils.ContextUtils;
import com.ginkgooai.core.project.aspect.lock.annotation.DistributedLock;
import com.ginkgooai.core.project.client.storage.StorageClient;
import com.ginkgooai.core.project.client.storage.dto.CloudFileResponse;
import com.ginkgooai.core.project.domain.application.Application;
import com.ginkgooai.core.project.domain.project.Project;
import com.ginkgooai.core.project.domain.project.ProjectStatus;
import com.ginkgooai.core.project.domain.role.ProjectRole;
import com.ginkgooai.core.project.domain.role.RoleStatus;
import com.ginkgooai.core.project.dto.request.ProjectCreateRequest;
import com.ginkgooai.core.project.dto.request.ProjectRolePatchRequest;
import com.ginkgooai.core.project.dto.request.ProjectRoleRequest;
import com.ginkgooai.core.project.dto.request.ProjectUpdateRequest;
import com.ginkgooai.core.project.dto.response.ProjectResponse;
import com.ginkgooai.core.project.dto.response.ProjectRoleResponse;
import com.ginkgooai.core.project.repository.ApplicationRepository;
import com.ginkgooai.core.project.repository.ProjectRepository;
import com.ginkgooai.core.project.repository.ProjectRoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    public ProjectResponse createProject(ProjectCreateRequest request) {
        String workspaceId = ContextUtils.getWorkspaceId();
        String userId = ContextUtils.getUserId();
        log.debug("Creating new project with request: {}", request);

        validateProjectRequest(request);

        Project project = new Project(request);

        // Initialize roles and bind to project
        List<CloudFileResponse> roleSideFiles = new ArrayList<>();
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            request.getRoles().forEach(roleRequest -> {
				ProjectRole role = ProjectRole.builder()
					.workspaceId(workspaceId)
					.name(roleRequest.getName())
                    .characterDescription(roleRequest.getCharacterDescription())
                    .selfTapeInstructions(roleRequest.getSelfTapeInstructions())
                    .isActive(roleRequest.getIsActive() != null ? roleRequest.getIsActive()
                        : true)
                    .sides(roleRequest.getSides().toArray(new String[0]))
                    .status(RoleStatus.DRAFTING).build();
                project.addRole(role);
            });

			List<String> sideFileIds = request.getRoles().stream().flatMap(roles -> roles.getSides().stream()).toList();
			if (!sideFileIds.isEmpty()) {
				roleSideFiles = ObjectUtils.isEmpty(sideFileIds) ? new ArrayList<>()
						: storageClient.getFileDetails(sideFileIds).getBody();
			}
            log.debug("Roles sides files: {}", roleSideFiles);
        }

        Project savedProject = projectRepository.save(project);
        savedProject.getRoles().forEach(role -> {
            activityLogger.log(workspaceId, savedProject.getId(), null, ActivityType.ROLE_CREATED,
                Map.of("user", userId, "project", savedProject.getName(), "roleName",
                    role.getName()),
                null, userId);
        });

        activityLogger.log(workspaceId, savedProject.getId(), null, ActivityType.PROJECT_CREATED,
				Map.of("project", savedProject.getName()), null, userId);

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
    @DistributedLock(key = "'project:' + #projectId")
    public Project updateProject(String projectId, ProjectUpdateRequest request,
                                 String workspaceId) {
        Project project = projectRepository.findByIdAndWorkspaceId(projectId, workspaceId)
            .orElseThrow(() -> new ResourceNotFoundException("Project", "id&workspaceId",
                projectId + ":" + workspaceId));

		if (project.getStatus() != request.getStatus()) {
			activityLogger.log(project.getWorkspaceId(), project.getId(), null, ActivityType.PROJECT_STATUS_CHANGE,
					Map.of("project", project.getName(), "previousStatus", project.getStatus().getValue(), "newStatus",
							request.getStatus().getValue()),
					null, project.getCreatedBy());
		}

        project.updateDetails(request.getName(), request.getDescription(), request.getPlotLine(),
            request.getStatus(), request.getPosterUrl(), request.getProducer());
        Project updatedProject = projectRepository.save(project);

        return updatedProject;
    }

    @Override
    @Transactional
    @DistributedLock(key = "'project:' + #projectId")
    public Project updateProjectStatus(String projectId, ProjectStatus status) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }

		ProjectStatus previousStatus = project.getStatus();
		if (status == previousStatus) {
            return project;
        }

        project.setStatus(status);
        Project updatedProject = projectRepository.save(project);
        activityLogger.log(project.getWorkspaceId(), project.getId(), null,
            ActivityType.PROJECT_STATUS_CHANGE,
            Map.of("project", updatedProject.getName(), "previousStatus",
						previousStatus.getValue(), "newStatus",
                updatedProject.getStatus().getValue()),
            null, updatedProject.getCreatedBy());

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
    public ProjectRoleResponse createRole(String projectId, ProjectRoleRequest request) {
        Project project = projectRepository.findById(projectId).orElseThrow(
            () -> new ResourceNotFoundException("Project", "projectId", projectId));

		String userId = ContextUtils.getUserId();
		ProjectRole role = new ProjectRole(project, request.getName(), request.getCharacterDescription(),
				request.getSelfTapeInstructions(), request.getSides().toArray(new String[0]));

        ProjectRole savedRole = projectRoleRepository.save(role);
        Map<String, CloudFileResponse> roleSideFilesMap = new HashMap<>();
        if (!ObjectUtils.isEmpty(savedRole.getSides())) {
            roleSideFilesMap = storageClient
                .getFileDetails(Arrays.stream(savedRole.getSides()).toList()).getBody().stream()
                .collect(Collectors.toMap(file -> file.getId(), Function.identity()));
        }

		activityLogger.log(project.getWorkspaceId(), project.getId(), null, ActivityType.ROLE_CREATED,
				Map.of("user", userId, "project", project.getName(), "roleName", role.getName()), null, userId);

        return ProjectRoleResponse.from(role, roleSideFilesMap);
    }

    @Override
    @Transactional
    public ProjectRole updateRole(String roleId, ProjectRoleRequest request) {
        ProjectRole role = projectRoleRepository.findById(roleId)
            .orElseThrow(() -> new ResourceNotFoundException("Role", "roleId", roleId));

        role.setName(request.getName());
        role.setCharacterDescription(request.getCharacterDescription());
        role.setSelfTapeInstructions(request.getSelfTapeInstructions());
		role.setStatus(request.getStatus());
		role.setSides(request.getSides().toArray(new String[0]));
		projectRoleRepository.save(role);

		return role;
    }

    @Override
    public ProjectRole patchRole(String roleId, ProjectRolePatchRequest request) {
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
        if (request.getStatus() != null) {
            role.setStatus(request.getStatus());
        }
        
		projectRoleRepository.save(role);

		return role;
    }

    @Override
    @Transactional
    public void deleteRole(String roleId) {
        ProjectRole role = projectRoleRepository.findById(roleId)
            .orElseThrow(() -> new ResourceNotFoundException("Role", "roleId", roleId));

        List<Application> dependentApplications = applicationRepository.findByRoleId(roleId);
        if (!dependentApplications.isEmpty()) {
            throw new ConflictException(
                String.format("Cannot delete role '%s' as it has %d dependent applications",
                    roleId, dependentApplications.size()));
        }

        applicationRepository.deleteByRoleId(roleId);

        projectRoleRepository.delete(role);
    }

}
