package com.ginkgooai.core.project.repository;

import com.ginkgooai.core.project.domain.application.Application;
import com.ginkgooai.core.project.domain.application.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, String>, JpaSpecificationExecutor<Application> {

    Optional<Application> findByIdAndWorkspaceId(String id, String workspaceId);

    List<Application> findByProjectIdOrderByCreatedAtDesc(String projectId);

    List<Application> findByProjectIdAndRoleIdOrderByCreatedAtDesc(
            String projectId,
            String roleId
    );

    List<Application> findByTalentEmailOrderByCreatedAtDesc(String talentEmail);

    List<Application> findByAgencyNameOrderByCreatedAtDesc(String agencyName);

    long countByProjectIdAndStatus(String projectId, ApplicationStatus status);

}
