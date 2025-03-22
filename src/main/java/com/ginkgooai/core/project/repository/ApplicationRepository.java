package com.ginkgooai.core.project.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ginkgooai.core.project.domain.application.Application;
import com.ginkgooai.core.project.domain.application.ApplicationStatus;
import com.ginkgooai.core.project.dto.response.ProjectRoleStatisticsResponse;

@Repository
public interface ApplicationRepository
        extends JpaRepository<Application, String>, JpaSpecificationExecutor<Application> {

    Optional<Application> findByIdAndWorkspaceId(String id, String workspaceId);

    List<Application> findByProjectIdOrderByCreatedAtDesc(String projectId);

    List<Application> findByProjectIdAndRoleIdOrderByCreatedAtDesc(String projectId, String roleId);

    List<Application> findByTalentEmailOrderByCreatedAtDesc(String talentEmail);

    long countByProjectIdAndStatus(String projectId, ApplicationStatus status);

    long countByRoleId(String roleId);

    long countByRoleIdAndStatus(String roleId, ApplicationStatus status);

    List<Application> findByRoleId(String roleId);

    @Query("""
            SELECT new com.ginkgooai.core.project.dto.response.ProjectRoleStatisticsResponse(
                a.role.id,
                a.role.name,
                a.role.sides,
                a.role.characterDescription,
                a.role.selfTapeInstructions,
                COUNT(a),
                COUNT(CASE WHEN a.status = 'ADDED' THEN 1 END),
                COUNT(CASE WHEN a.status = 'SUBMITTED' THEN 1 END),
                COUNT(CASE WHEN a.status = 'SHORTLISTED' THEN 1 END),
                COUNT(CASE WHEN a.status = 'DECLINED' THEN 1 END)
            )
            FROM Application a
            WHERE a.role.id = :roleId
            GROUP BY a.role.id, a.role.name, a.role.sides, a.role.characterDescription, a.role.selfTapeInstructions
            """)
    ProjectRoleStatisticsResponse getRoleStatistics(@Param("roleId") String roleId);

    @Query("""
            SELECT new com.ginkgooai.core.project.dto.response.ProjectRoleStatisticsResponse(
                r.id,
                r.name,
                r.sides,
                r.characterDescription,
                r.selfTapeInstructions,
                COUNT(a),
                COUNT(CASE WHEN a.status = 'ADDED' THEN 1 END),
                COUNT(CASE WHEN a.status = 'SUBMITTED' THEN 1 END),
                COUNT(CASE WHEN a.status = 'SHORTLISTED' THEN 1 END),
                COUNT(CASE WHEN a.status = 'DECLINED' THEN 1 END)
            )
            FROM Application a
            RIGHT JOIN a.role r
            WHERE r.project.id = :projectId
            GROUP BY r.id, r.name, r.sides, r.characterDescription, r.selfTapeInstructions
            """)
    List<ProjectRoleStatisticsResponse> getProjectRolesStatistics(@Param("projectId") String projectId);
}
