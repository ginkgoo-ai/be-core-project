package com.ginkgooai.core.project.repository;

import com.ginkgooai.core.project.domain.application.ShortlistItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface ShortlistItemRepository extends JpaRepository<ShortlistItem, String>, JpaSpecificationExecutor<ShortlistItem> {

    @Query("SELECT si FROM ShortlistItem si WHERE si.shortlist.id = :shortlistId")
    Page<ShortlistItem> findShortlistItems(Specification<ShortlistItem> spec, Pageable pageable);
}