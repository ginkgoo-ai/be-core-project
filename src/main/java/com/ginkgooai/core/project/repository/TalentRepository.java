package com.ginkgooai.core.project.repository;

import com.ginkgooai.core.project.domain.talent.Talent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TalentRepository extends JpaRepository<Talent, String>, JpaSpecificationExecutor<Talent> {
    /**
     * Find all talents by workspace ID
     *
     * @param workspaceId The workspace ID
     * @return List of talents in the workspace
     */
    List<Talent> findByWorkspaceId(String workspaceId);

    /**
     * Find talents by workspace ID and name matching either first name or last name
     *
     * @param workspaceId The workspace ID
     * @param name        The name to search for in both first name and last name
     * @return List of matching talents
     */
    @Query("SELECT t FROM Talent t WHERE t.workspaceId = :workspaceId AND " +
        "(LOWER(t.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
        "LOWER(t.lastName) LIKE LOWER(CONCAT('%', :name, '%')))")
    List<Talent> findByWorkspaceIdAndNameMatching(
        @Param("workspaceId") String workspaceId,
        @Param("name") String name);
}