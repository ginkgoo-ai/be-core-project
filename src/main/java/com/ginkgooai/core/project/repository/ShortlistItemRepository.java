package com.ginkgooai.core.project.repository;

import com.ginkgooai.core.project.domain.application.ShortlistItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ShortlistItemRepository extends JpaRepository<ShortlistItem, String>, JpaSpecificationExecutor<ShortlistItem> {

    Optional<ShortlistItem> findByApplicationIdAndShortlistId(String applicationId, String shortListId);

    @Query(value = """
            SELECT si.* FROM shortlist_item si 
            INNER JOIN shortlist_item_submission_mapping sm ON si.id = sm.shortlist_item_id 
            WHERE sm.submission_id = :submissionId AND si.owner_id = :ownerId
            """, nativeQuery = true)
    List<ShortlistItem> findAllBySubmissionId(@Param("submissionId") String submissionId, @Param("ownerId") String ownerId);
}