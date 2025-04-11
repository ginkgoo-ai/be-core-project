package com.ginkgooai.core.project.repository;

import com.ginkgooai.core.project.domain.application.Application;
import com.ginkgooai.core.project.domain.application.ApplicationStatus;
import com.ginkgooai.core.project.dto.response.ProjectRoleStatisticsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository
                extends JpaRepository<Application, String>, JpaSpecificationExecutor<Application> {

        Optional<Application> findByIdAndWorkspaceId(String id, String workspaceId);

        List<Application> findByTalentIdOrderByCreatedAtDesc(String talentId);
        
        List<Application> findByProjectIdOrderByCreatedAtDesc(String projectId);

        List<Application> findByProjectIdAndRoleIdOrderByCreatedAtDesc(String projectId, String roleId);

        List<Application> findByTalentEmailOrderByCreatedAtDesc(String talentEmail);

        long countByWorkspaceIdAndStatus(String workspaceId, ApplicationStatus status);

        long countByRoleId(String roleId);

        long countByRoleIdAndStatus(String roleId, ApplicationStatus status);

        List<Application> findByRoleId(String roleId);

        @Query("""
                        SELECT new com.ginkgooai.core.project.dto.response.ProjectRoleStatisticsResponse(
                r.id as id,
                r.name as name,
                r.status,
                r.sides,
                r.characterDescription,
                r.selfTapeInstructions,
                COUNT(r) as total,
                            COUNT(CASE WHEN a.status = 'ADDED' THEN 1 END) as added,
                            COUNT(CASE WHEN a.status = 'SUBMITTED' THEN 1 END) as submitted,
                            COUNT(CASE WHEN a.status = 'SHORTLISTED' THEN 1 END) as shortlisted,
                            COUNT(CASE WHEN a.status = 'DECLINED' THEN 1 END) as declined
                        )
            FROM ProjectRole r LEFT JOIN Application a on a.role.id = r.id
            GROUP BY a.role.id, a.role.name, a.role.status, a.role.sides, a.role.characterDescription, a.role.selfTapeInstructions
                        """)
        ProjectRoleStatisticsResponse getRoleStatistics(@Param("roleId") String roleId);

        @Query("""
                        SELECT new com.ginkgooai.core.project.dto.response.ProjectRoleStatisticsResponse(
                            r.id as id,
                            r.name as name,
                            r.status,
                            r.sides,
                            r.characterDescription,
                            r.selfTapeInstructions,
                            COUNT(r) as total,
                            COUNT(CASE WHEN a.status = 'ADDED' THEN 1 END) as added,
                            COUNT(CASE WHEN a.status = 'SUBMITTED' THEN 1 END) as submitted,
                            COUNT(CASE WHEN a.status = 'SHORTLISTED' THEN 1 END) as shortlisted,
                            COUNT(CASE WHEN a.status = 'DECLINED' THEN 1 END) as declined
                        )
                        FROM ProjectRole r
                        LEFT JOIN Application a on a.role.id = r.id
                        WHERE r.project.id = :projectId
                        AND (COALESCE(:name, '') = '' OR r.name LIKE CONCAT('%', :name, '%'))
            GROUP BY r.id, r.name, r.status, r.sides, r.characterDescription, r.selfTapeInstructions
                        """)
        Page<ProjectRoleStatisticsResponse> getProjectRolesStatistics(@Param("projectId") String projectId,
                        @Param("name") String name, Pageable pageable);

        @Modifying
        void deleteByRoleId(String roleId);

        /**
         * Count applications by status for a specific project
         *
         * @param projectId The project ID
         * @return Map of ApplicationStatus to count
         */
        @Query("SELECT a.status as status, COUNT(a) as count FROM Application a WHERE a.project.id = :projectId GROUP BY a.status")
        List<Object[]> countByProjectIdGroupByStatus(@Param("projectId") String projectId);
}
