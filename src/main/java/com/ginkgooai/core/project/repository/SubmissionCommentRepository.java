package com.ginkgooai.core.project.repository;

import com.ginkgooai.core.project.domain.application.SubmissionComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SubmissionCommentRepository extends JpaRepository<SubmissionComment, String>, JpaSpecificationExecutor<SubmissionComment> {
}