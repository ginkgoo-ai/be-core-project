package com.ginkgooai.core.project.repository;

import com.ginkgooai.core.project.domain.application.ShortlistShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ShortlistShareRepository extends JpaRepository<ShortlistShare, String> {

	List<ShortlistShare> findByShortlistIdAndActiveTrue(String shortlistId);

	Optional<ShortlistShare> findByShareCode(String shareCode);

	@Query("SELECT s FROM ShortlistShare s WHERE s.shortlist.id = :shortlistId " + "AND s.recipientEmail = :email "
			+ "AND s.active = true " + "AND s.expiresAt > CURRENT_TIMESTAMP "
			+ "AND s.expiresAt > DATEADD(HOUR, 1, CURRENT_TIMESTAMP)")
	Optional<ShortlistShare> findActiveShareByShortlistIdAndEmail(@Param("shortlistId") String shortlistId,
			@Param("email") String email);

}