package com.ginkgooai.core.project.service;

import com.ginkgooai.core.project.domain.ProjectRole;
import com.ginkgooai.core.project.domain.ProjectStatus;
import com.ginkgooai.core.project.dto.request.ProjectResponse;
import com.ginkgooai.core.project.dto.request.ProjectRolePatchRequest;
import com.ginkgooai.core.project.dto.response.ProjectBasicResponse;
import com.ginkgooai.core.project.dto.response.ProjectRoleStatisticsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProjectReadService {

    Optional<ProjectResponse> findById(String id);

    Page<ProjectResponse> findProjects(String name, ProjectStatus status, Pageable pageable);

    List<ProjectBasicResponse> findAllBasicInfo();

    List<ProjectResponse> findAll();

    List<ProjectResponse> findByOwnerId(String ownerId);

    List<ProjectResponse> findByStatus(ProjectStatus status);

    Optional<ProjectRole> findRoleById(String roleId);

    ProjectRoleStatisticsResponse getRoleStatistics(String roleId);

    List<ProjectRole> findRolesByProjectId(String projectId);

}