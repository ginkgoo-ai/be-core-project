package com.ginkgooai.core.project.domain.application;

import com.ginkgooai.core.project.domain.BaseAuditableEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "shortlist_share")
public class ShortlistShare extends BaseAuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "shortlist_id")
	private Shortlist shortlist;

	@Schema(description = "Share link", example = "https://example.com/shared/abc123")
	private String shareLink;

	private String recipientId;

	private String recipientEmail;

	private String recipientName;

	private String shareCode;

	private LocalDateTime expiresAt;

	private boolean active;

}