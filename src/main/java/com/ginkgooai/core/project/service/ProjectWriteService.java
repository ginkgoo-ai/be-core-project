package com.ginkgooai.core.project.service;

import com.ginkgooai.core.project.domain.*;
import com.ginkgooai.core.project.dto.request.*;

public interface ProjectWriteService {

    Project createProject(ProjectRequest request, String workspaceId);

    Project updateProject(String id, ProjectRequest request);

    void deleteProject(String id);

    ProjectRole createRole(String projectId, ProjectRoleRequest request);

    ProjectNda createNda(String projectId, ProjectNdaRequest request);

    ProjectMember addMember(String projectId, ProjectMemberRequest request);

    ProjectActivity logActivity(String projectId, ProjectActivityRequest request);
}