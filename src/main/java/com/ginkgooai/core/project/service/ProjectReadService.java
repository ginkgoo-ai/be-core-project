package com.ginkgooai.core.project.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ginkgooai.core.project.domain.project.ProjectStatus;
import com.ginkgooai.core.project.domain.role.ProjectRole;
import com.ginkgooai.core.project.dto.request.ProjectResponse;
import com.ginkgooai.core.project.dto.response.ProjectBasicResponse;
import com.ginkgooai.core.project.dto.response.ProjectRoleStatisticsResponse;

public interface ProjectReadService {

    Optional<ProjectResponse> findById(String workspaceId, String id);

    Page<ProjectResponse> findProjects(String workspaceId, String name, ProjectStatus status, Pageable pageable);

    List<ProjectBasicResponse> findAllBasicInfo();

    List<ProjectResponse> findAll();

    List<ProjectResponse> findByOwnerId(String ownerId);

    List<ProjectResponse> findByStatus(ProjectStatus status);

    Optional<ProjectRole> findRoleById(String roleId);

    ProjectRoleStatisticsResponse getRoleStatistics(String roleId);

    List<ProjectRole> findRolesByProjectId(String projectId);

    Page<ProjectRole> findRolesByProjectIdPaginated(String projectId, Pageable pageable);

    /**
     * 获取项目中所有角色的统计数据
     * 
     * @param projectId 项目ID
     * @return 角色统计数据列表
     */
    List<ProjectRoleStatisticsResponse> getProjectRolesStatistics(String projectId);

}