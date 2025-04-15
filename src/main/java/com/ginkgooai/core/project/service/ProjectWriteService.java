package com.ginkgooai.core.project.service;

import com.ginkgooai.core.project.domain.project.Project;
import com.ginkgooai.core.project.domain.project.ProjectStatus;
import com.ginkgooai.core.project.domain.role.ProjectRole;
import com.ginkgooai.core.project.dto.request.ProjectCreateRequest;
import com.ginkgooai.core.project.dto.request.ProjectRolePatchRequest;
import com.ginkgooai.core.project.dto.request.ProjectRoleRequest;
import com.ginkgooai.core.project.dto.request.ProjectUpdateRequest;
import com.ginkgooai.core.project.dto.response.ProjectResponse;
import com.ginkgooai.core.project.dto.response.ProjectRoleResponse;

public interface ProjectWriteService {

    ProjectResponse createProject(ProjectCreateRequest request);

    Project updateProject(String id, ProjectUpdateRequest request, String workspaceId);

    Project updateProjectStatus(String id, ProjectStatus status);

    void deleteProject(String id);

	ProjectRoleResponse createRole(String projectId, ProjectRoleRequest request);

    ProjectRole updateRole(String roleId, ProjectRoleRequest request);

    ProjectRole patchRole(String roleId, ProjectRolePatchRequest request);

    void deleteRole(String roleId);
}