package com.ginkgooai.core.project.repository;

import com.ginkgooai.core.project.domain.application.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubmissionRepository extends JpaRepository<Submission, String>, JpaSpecificationExecutor<Submission> {
    @Query("""
        SELECT s FROM Submission s 
        WHERE s.application.talent.id = :talentId 
        ORDER BY s.createdAt DESC
        """)
    List<Submission> findByTalentIdOrderByCreatedAtDesc(@Param("talentId") String talentId);

    @Query("""
        SELECT COUNT(s) FROM Submission s 
        WHERE s.workspaceId = :workspaceId 
        AND (s.viewCount = 0 OR s.viewCount IS NULL)
        """)
    long countUnviewedSubmissions(@Param("workspaceId") String workspaceId);
}