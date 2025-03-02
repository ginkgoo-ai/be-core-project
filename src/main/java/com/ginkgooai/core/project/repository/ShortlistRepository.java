package com.ginkgooai.core.project.repository;

import com.ginkgooai.core.project.domain.application.Shortlist;
import com.ginkgooai.core.project.domain.talent.Talent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import javax.swing.text.html.Option;
import java.util.Optional;

public interface ShortlistRepository extends JpaRepository<Shortlist, String>, JpaSpecificationExecutor<Shortlist> {

    Optional<Shortlist> findByWorkspaceIdAndProjectIdAndOwnerId(String workspaceId, String projectId, String ownerId);
}