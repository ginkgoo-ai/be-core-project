package com.ginkgooai.core.project.repository;

import com.ginkgooai.core.project.domain.project.Project;
import com.ginkgooai.core.project.domain.project.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, String>, JpaSpecificationExecutor<Project> {
    Optional<Project> findByIdAndWorkspaceId(String id, String workspaceId);

    List<Project> findByCreatedBy(String ownerId);

    List<Project> findByWorkspaceId(String workspaceId);

    List<Project> findByStatus(ProjectStatus status);

    List<Project> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);

    List<Project> findByCreatedAtGreaterThanEqualAndCreatedAtLessThanEqual(
            LocalDateTime startTime, LocalDateTime endTime);

    long countByWorkspaceIdAndStatusIn(String workspaceId, List<ProjectStatus> statuses);
}