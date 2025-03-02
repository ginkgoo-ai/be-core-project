package com.ginkgooai.core.project.repository;

import com.ginkgooai.core.project.domain.project.Project;
import com.ginkgooai.core.project.domain.project.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, String>, JpaSpecificationExecutor<Project> {
    List<Project> findByOwnerId(String ownerId);
    List<Project> findByStatus(ProjectStatus status);
}