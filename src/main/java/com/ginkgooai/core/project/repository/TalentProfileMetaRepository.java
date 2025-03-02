package com.ginkgooai.core.project.repository;

import com.ginkgooai.core.project.domain.talent.TalentProfileMeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TalentProfileMetaRepository extends JpaRepository<TalentProfileMeta, String>, JpaSpecificationExecutor<TalentProfileMeta> {
}