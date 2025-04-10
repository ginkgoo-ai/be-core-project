package com.ginkgooai.core.project.repository;

import com.ginkgooai.core.project.domain.talent.TalentComment;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TalentCommentRepository extends JpaRepository<TalentComment, String>, JpaSpecificationExecutor<TalentComment> {


    /**
     * Find all comments for a talent in a workspace
     */
    List<TalentComment> findByWorkspaceIdAndTalentId(String workspaceId, String talentId, Sort sort);

    Optional<TalentComment> findByWorkspaceIdAndId(String workspaceId, String id);
}