package com.ginkgooai.core.project.repository;

import com.ginkgooai.core.project.domain.talent.ImdbMovieItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface ImdbMovieItemRepository extends JpaRepository<ImdbMovieItem, String>, JpaSpecificationExecutor<ImdbMovieItem> {

    @Query("SELECT i.titleUrl FROM ImdbMovieItem i WHERE i.titleUrl IN :titleUrls")
    Set<String> findExistingTitleUrls(@Param("titleUrls") Set<String> titleUrls);
    
}