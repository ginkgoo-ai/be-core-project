package com.ginkgooai.core.project.repository;

import com.ginkgooai.core.project.domain.talent.Talent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TalentRepository extends JpaRepository<Talent, String>, JpaSpecificationExecutor<Talent> {
}