package com.ginkgooai.core.project.repository;

import com.ginkgooai.core.project.domain.project.Project;
import com.ginkgooai.core.project.domain.project.ProjectStatus;
import com.ginkgooai.core.project.dto.response.ProjectListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, String>, JpaSpecificationExecutor<Project> {

	@Query(value = """
			SELECT new com.ginkgooai.core.project.dto.response.ProjectListResponse(
			    p.id,
			    p.name,
			    p.status,
			    (SELECT COUNT(*) FROM ProjectRole pr WHERE pr.project.id = p.id) as roleCount,
			    (SELECT COUNT(*) FROM Application a WHERE a.project.id = p.id AND a.status = 'SUBMITTED') as pendingReviewCount,
			    p.updatedAt,
			    p.posterUrl,
			    p.producer
			)
			FROM Project p
			WHERE p.workspaceId = :workspaceId
			AND (:nameLike IS NULL OR p.name ILIKE CONCAT('%', :nameLike, '%'))
			AND (:status IS NULL OR p.status = :status)
			""")
	Page<ProjectListResponse> findProjectList(@Param("workspaceId") String workspaceId,
			@Param("nameLike") String nameLike, @Param("status") ProjectStatus status, Pageable pageable);
    
    Optional<Project> findByIdAndWorkspaceId(String id, String workspaceId);

    List<Project> findByCreatedBy(String ownerId);

    List<Project> findByWorkspaceId(String workspaceId);

    List<Project> findByStatus(ProjectStatus status);

    List<Project> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);

    List<Project> findByCreatedAtGreaterThanEqualAndCreatedAtLessThanEqual(
            LocalDateTime startTime, LocalDateTime endTime);

    long countByWorkspaceIdAndStatusIn(String workspaceId, List<ProjectStatus> statuses);
}