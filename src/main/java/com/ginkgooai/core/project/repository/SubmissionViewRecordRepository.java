package com.ginkgooai.core.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ginkgooai.core.project.domain.application.SubmissionViewRecord;

public interface SubmissionViewRecordRepository
                extends JpaRepository<SubmissionViewRecord, String>, JpaSpecificationExecutor<SubmissionViewRecord> {

        /**
         * Check if a specific user or IP has already viewed a video
         * 
         * @param submissionId submission ID
         * @param userId       user ID
         * @return whether a view record exists
         */
        @Query("SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END FROM SubmissionViewRecord v " +
                        "WHERE v.submission.id = :submissionId " +
                        "AND (:userId IS NOT NULL AND v.userId = :userId)")
        boolean existsBySubmissionIdAndUserIdOrIpAddress(
                        @Param("submissionId") String submissionId,
                        @Param("userId") String userId);
}