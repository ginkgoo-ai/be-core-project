package com.ginkgooai.core.project.repository;

import com.ginkgooai.core.project.domain.ProjectActivity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectActivityRepository extends JpaRepository<ProjectActivity, String> {
}