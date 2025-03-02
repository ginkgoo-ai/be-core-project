package com.ginkgooai.core.project.repository;

import com.ginkgooai.core.project.domain.application.ShortlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ShortlistItemRepository extends JpaRepository<ShortlistItem, String>, JpaSpecificationExecutor<ShortlistItem> {
}