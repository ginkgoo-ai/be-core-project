package com.ginkgooai.core.project.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.ginkgooai.core.project.domain.project.Project;
import com.ginkgooai.core.project.domain.project.ProjectStatus;
import com.ginkgooai.core.project.domain.role.ProjectRole;
import com.ginkgooai.core.project.dto.request.ProjectResponse;
import com.ginkgooai.core.project.dto.response.ProjectBasicResponse;
import com.ginkgooai.core.project.dto.response.ProjectRoleStatisticsResponse;
import com.ginkgooai.core.project.repository.ApplicationRepository;
import com.ginkgooai.core.project.repository.ProjectRepository;
import com.ginkgooai.core.project.repository.ProjectRoleRepository;
import com.ginkgooai.core.project.specification.ProjectSpecification;

@Service
public class ProjectReadServiceImpl implements ProjectReadService {

    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private ProjectRoleRepository projectRoleRepository;
    @Autowired
    private ApplicationRepository applicationRepository;

    @Override
    public Optional<ProjectResponse> findById(String workspaceId, String id) {
        return projectRepository.findByIdAndWorkspaceId(id, workspaceId)
                .map(ProjectResponse::from);
    }

    @Override
    public List<ProjectResponse> findAll() {
        return projectRepository.findAll().stream()
                .map(ProjectResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ProjectResponse> findProjects(String workspaceId, String name, ProjectStatus status,
            Pageable pageable) {
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
        return projects.map(ProjectResponse::from);
    }

    @Override
    public List<ProjectBasicResponse> findAllBasicInfo() {
        return projectRepository.findAll().stream()
                .map(ProjectBasicResponse::fromProject)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProjectResponse> findByOwnerId(String ownerId) {
        return projectRepository.findByCreatedBy(ownerId).stream()
                .map(ProjectResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProjectResponse> findByStatus(ProjectStatus status) {
        return projectRepository.findByStatus(status).stream()
                .map(ProjectResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ProjectRole> findRoleById(String roleId) {
        return projectRoleRepository.findById(roleId);
    }

    @Override
    public ProjectRoleStatisticsResponse getRoleStatistics(String roleId) {
        return applicationRepository.getRoleStatistics(roleId);
    }

    @Override
    public List<ProjectRoleStatisticsResponse> getProjectRolesStatistics(String projectId) {
        return applicationRepository.getProjectRolesStatistics(projectId);
    }

    @Override
    public List<ProjectRole> findRolesByProjectId(String projectId) {
        return projectRoleRepository.findByProjectId(projectId);
    }

    @Override
    public Page<ProjectRole> findRolesByProjectIdPaginated(String projectId, Pageable pageable) {
        return projectRoleRepository.findByProjectId(projectId, pageable);
    }

}