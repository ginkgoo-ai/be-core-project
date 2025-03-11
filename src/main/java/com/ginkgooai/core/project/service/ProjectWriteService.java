package com.ginkgooai.core.project.service;

import com.ginkgooai.core.project.domain.project.*;
import com.ginkgooai.core.project.domain.role.ProjectRole;
import com.ginkgooai.core.project.dto.request.*;

public interface ProjectWriteService {

    Project createProject(ProjectRequest request, String workspaceId, String userId);

    Project updateProject(String id, ProjectRequest request, String workspaceId);

    Project updateProjectStatus(String id, ProjectStatus status);

    void deleteProject(String id);

    ProjectRole createRole(String projectId, ProjectRoleRequest request);

    ProjectRole updateRole(String roleId, ProjectRoleRequest request);

    ProjectRole patchRoleDetails(String roleId, ProjectRolePatchRequest request);

    void deleteRole(String roleId);

    ProjectNda createNda(String projectId, ProjectNdaRequest request);

    ProjectMember addMember(String projectId, ProjectMemberRequest request);
}