package com.ginkgooai.core.project.repository;

import com.ginkgooai.core.project.domain.role.ProjectRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRoleRepository extends JpaRepository<ProjectRole, String> {
    //find by projectId
    List<ProjectRole> findByProjectId(String projectId);

    Page<ProjectRole> findByProjectId(String projectId, Pageable pageable);
}