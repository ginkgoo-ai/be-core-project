package com.ginkgooai.core.project.repository;

import com.ginkgooai.core.project.domain.talent.TalentProfileMeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface TalentProfileMetaRepository extends JpaRepository<TalentProfileMeta, String>, JpaSpecificationExecutor<TalentProfileMeta> {

    Optional<TalentProfileMeta> findBySourceUrl(String sourceUrl);

}