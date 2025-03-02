package com.ginkgooai.core.project.repository;

import com.ginkgooai.core.project.domain.talent.ImdbMovieItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ImdbMovieItemRepository extends JpaRepository<ImdbMovieItem, String>, JpaSpecificationExecutor<ImdbMovieItem> {
}