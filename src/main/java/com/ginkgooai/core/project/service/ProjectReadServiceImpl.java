package com.ginkgooai.core.project.service;

import com.ginkgooai.core.project.domain.project.*;
import com.ginkgooai.core.project.dto.request.ProjectResponse;
import com.ginkgooai.core.project.dto.response.ProjectBasicResponse;
import com.ginkgooai.core.project.dto.response.ProjectRoleStatisticsResponse;
import com.ginkgooai.core.project.repository.ProjectRepository;
import com.ginkgooai.core.project.repository.ProjectRoleRepository;
import com.ginkgooai.core.project.specification.ProjectSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProjectReadServiceImpl implements ProjectReadService {

    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private ProjectRoleRepository projectRoleRepository;

    @Override
    public Optional<ProjectResponse> findById(String workspaceId, String id) {
        return projectRepository.findByIdAndWorkspaceId(id, workspaceId)
                .map(this::mapToResponse);
    }

    @Override
    public List<ProjectResponse> findAll() {
        return projectRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ProjectResponse> findProjects(String workspaceId, String name, ProjectStatus status, Pageable pageable) {
        Specification<Project> spec = Specification.where(ProjectSpecification.hasWorkspaceId(workspaceId));
        
        // Apply name filter if provided and not empty
        if (name != null && !name.trim().isEmpty()) {
            spec = spec.and(ProjectSpecification.hasNameLike(name.trim()));
        }

        // Apply status filter if provided
        if (status != null) {
            spec = spec.and(ProjectSpecification.hasStatus(status));
        }

        Page<Project> projects = projectRepository.findAll(spec, pageable);
        return projects.map(this::mapToResponse);
    }

    @Override
    public List<ProjectBasicResponse> findAllBasicInfo() {
        return projectRepository.findAll().stream()
                .map(ProjectBasicResponse::fromProject)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProjectResponse> findByOwnerId(String ownerId) {
        return projectRepository.findByOwnerId(ownerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProjectResponse> findByStatus(ProjectStatus status) {
        return projectRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ProjectRole> findRoleById(String roleId) {
        return projectRoleRepository.findById(roleId); 
    }

    @Override
    public ProjectRoleStatisticsResponse getRoleStatistics(String roleId) {
        return new ProjectRoleStatisticsResponse();
    }

    @Override
    public List<ProjectRole> findRolesByProjectId(String projectId) {
        return projectRoleRepository.findByProjectId(projectId);
    }

    private ProjectResponse mapToResponse(Project project) {
        ProjectResponse response = new ProjectResponse();
        response.setId(project.getId());
        response.setName(project.getName());
        response.setDescription(project.getDescription());
        response.setPlotLine(project.getPlotLine());
        response.setStatus(project.getStatus());
        response.setOwnerId(project.getOwnerId());
        response.setLastActivityAt(project.getLastActivityAt());
        response.setCreatedAt(project.getCreatedAt());
        response.setUpdatedAt(project.getUpdatedAt());
        response.setRoleIds(project.getRoles().stream().map(ProjectRole::getId).collect(Collectors.toSet()));
        response.setNdaIds(project.getNdas().stream().map(ProjectNda::getId).collect(Collectors.toSet()));
        response.setMemberIds(project.getMembers().stream().map(ProjectMember::getId).collect(Collectors.toSet()));
        response.setWorkspaceId(project.getWorkspaceId());
        return response;
    }
}