package com.ginkgooai.core.project.repository;

import com.ginkgooai.core.project.domain.role.ProjectRole;
import com.ginkgooai.core.project.domain.role.RoleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRoleRepository extends JpaRepository<ProjectRole, String> {
    //find by projectId
    List<ProjectRole> findByProjectId(String projectId);

    List<ProjectRole> findByProjectIdAndStatus(String projectId, RoleStatus status);

    Page<ProjectRole> findByProjectId(String projectId, Pageable pageable);

    long countByWorkspaceIdAndStatusNot(String workspaceId, RoleStatus status);
}