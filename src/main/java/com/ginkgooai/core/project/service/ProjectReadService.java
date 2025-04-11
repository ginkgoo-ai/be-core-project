package com.ginkgooai.core.project.service;

import com.ginkgooai.core.project.domain.project.ProjectStatus;
import com.ginkgooai.core.project.domain.role.ProjectRole;
import com.ginkgooai.core.project.dto.request.ProjectResponse;
import com.ginkgooai.core.project.dto.response.ProjectBasicResponse;
import com.ginkgooai.core.project.dto.response.ProjectRoleStatisticsResponse;
import com.ginkgooai.core.project.dto.response.ProjectStatisticsResponse;
import com.ginkgooai.core.project.dto.response.RoleBasicResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProjectReadService {

    Optional<ProjectResponse> findById(String workspaceId, String id);

    Page<ProjectResponse> findProjects(String workspaceId, String name, ProjectStatus status, Pageable pageable);

    List<ProjectBasicResponse> findAllBasicInfo();

    ProjectStatisticsResponse getProjectsStatistics();

    List<ProjectResponse> findAll();

    List<ProjectResponse> findByOwnerId(String ownerId);

    List<ProjectResponse> findByStatus(ProjectStatus status);

    Optional<ProjectRole> findRoleById(String roleId);

    ProjectRoleStatisticsResponse getRoleStatistics(String roleId);

    List<ProjectRole> findRolesByProjectId(String projectId);

    Page<ProjectRole> findRolesByProjectIdPaginated(String projectId, Pageable pageable);

    Page<ProjectRoleStatisticsResponse> getProjectRolesStatistics(String projectId, String name, Pageable pageable);

    List<RoleBasicResponse> findAllRolesBasicInfo(String projectId);
}