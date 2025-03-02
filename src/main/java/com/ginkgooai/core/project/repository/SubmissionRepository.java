package com.ginkgooai.core.project.repository;

import com.ginkgooai.core.project.domain.application.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SubmissionRepository extends JpaRepository<Submission, String>, JpaSpecificationExecutor<Submission> {
}