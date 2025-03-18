package com.ginkgooai.core.project.repository;

import com.ginkgooai.core.project.domain.application.ApplicationNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationNoteRepository extends JpaRepository<ApplicationNote, String>, JpaSpecificationExecutor<ApplicationNote> {

}
