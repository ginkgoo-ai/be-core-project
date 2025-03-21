package com.ginkgooai.core.project.service;

import com.ginkgooai.core.common.exception.ResourceNotFoundException;
import com.ginkgooai.core.project.domain.project.*;
import com.ginkgooai.core.project.domain.role.ProjectRole;
import com.ginkgooai.core.project.dto.request.*;
import com.ginkgooai.core.project.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
public class ProjectWriteServiceImpl implements ProjectWriteService {

    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private ProjectRoleRepository projectRoleRepository;

    @Override
    @Transactional
    public Project createProject(ProjectCreateRequest request, String workspaceId, String userId) {
        log.debug("Creating new project with request: {}", request);

        validateProjectRequest(request);

        Project project = new Project(request, workspaceId, userId);
        Project savedProject = projectRepository.save(project);

        // Set optional status if provided (override default DRAFTING if necessary)
        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
        }

        // Initialize roles and bind to project
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            request.getRoles().forEach(roleRequest -> {
                ProjectRole role = ProjectRole.builder()
                        .name(roleRequest.getName())
                        .characterDescription(roleRequest.getCharacterDescription())
                        .selfTapeInstructions(roleRequest.getSelfTapeInstructions())
                        .isActive(roleRequest.getIsActive() != null ? roleRequest.getIsActive() : true)
                        .sides(roleRequest.getSides())
                        .project(savedProject)
                        .build();
                projectRoleRepository.save(role);
            });
        }

        return savedProject;
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
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id&workspaceId", projectId + ":" + workspaceId));

        project.updateDetails(request.getName(), request.getDescription(), request.getPlotLine(), request.getStatus(), request.getPosterUrl());

        return projectRepository.save(project);
    }

    @Override
    @Transactional
    public Project updateProjectStatus(String id, ProjectStatus status) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));

        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }

        project.setStatus(status);
        project.setLastActivityAt(LocalDateTime.now());

        return projectRepository.save(project);
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
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new ResourceNotFoundException("Project", "projectId", projectId));

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
            role.setSides(request.getSides());
        }
        return projectRoleRepository.save(role);
    }

    @Override
    @Transactional
    public void deleteRole(String roleId) {
        ProjectRole role = projectRoleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "roleId", roleId));

        Project project = role.getProject();
        project.removeRole(roleId);

        projectRepository.save(project);
        projectRoleRepository.delete(role);
    }

}