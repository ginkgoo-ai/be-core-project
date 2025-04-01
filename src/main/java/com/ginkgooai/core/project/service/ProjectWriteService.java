package com.ginkgooai.core.project.service;

import com.ginkgooai.core.project.domain.project.Project;
import com.ginkgooai.core.project.domain.project.ProjectStatus;
import com.ginkgooai.core.project.domain.role.ProjectRole;
import com.ginkgooai.core.project.dto.request.*;
import com.ginkgooai.core.project.dto.response.ProjectRoleResponse;

public interface ProjectWriteService {

    ProjectResponse createProject(ProjectCreateRequest request, String workspaceId, String userId);

    Project updateProject(String id, ProjectUpdateRequest request, String workspaceId);

    Project updateProjectStatus(String id, ProjectStatus status);

    void deleteProject(String id);

	ProjectRoleResponse createRole(String projectId, ProjectRoleRequest request);

    ProjectRole updateRole(String roleId, ProjectRoleRequest request);

    ProjectRole patchRoleDetails(String roleId, ProjectRolePatchRequest request);

    void deleteRole(String roleId);
}