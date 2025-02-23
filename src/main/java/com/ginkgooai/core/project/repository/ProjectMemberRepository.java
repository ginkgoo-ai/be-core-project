package com.ginkgooai.core.project.repository;

import com.ginkgooai.core.project.domain.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, String> {
}