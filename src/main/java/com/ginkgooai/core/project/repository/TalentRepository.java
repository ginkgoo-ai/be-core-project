package com.ginkgooai.core.project.repository;

import com.ginkgooai.core.project.domain.talent.Talent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface TalentRepository extends JpaRepository<Talent, String>, JpaSpecificationExecutor<Talent> {
    /**
     * Find all talents by workspace ID
     *
     * @param workspaceId The workspace ID
     * @return List of talents in the workspace
     */
    List<Talent> findByWorkspaceId(String workspaceId);
}