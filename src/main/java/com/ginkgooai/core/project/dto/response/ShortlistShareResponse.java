package com.ginkgooai.core.project.dto.response;

import com.ginkgooai.core.project.domain.application.ShortlistShare;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing shortlist share information")
public class ShortlistShareResponse {

	@Schema(description = "Share ID", example = "abc123")
	private String id;

	@Schema(description = "Recipient id", example = "user123145")
	private String recipientId;

	@Schema(description = "Recipient email", example = "user@example.com")
	private String recipientEmail;

	@Schema(description = "Recipient name", example = "John Doe")
	private String recipientName;

	@Schema(description = "Share link", example = "https://example.com/shared/abc123")
	private String shareLink;

	@Schema(description = "Expiration date", example = "2023-12-31T23:59:59")
	private LocalDateTime expiresAt;

	@Schema(description = "Whether the share is active", example = "true")
	private boolean active;

	public static ShortlistShareResponse from(ShortlistShare share) {
		return ShortlistShareResponse.builder()
			.id(share.getId())
			.recipientEmail(share.getRecipientEmail())
			.recipientName(share.getRecipientName())
			.recipientId(share.getRecipientId())
			.shareLink(share.getShareLink())
			.expiresAt(share.getExpiresAt())
			.active(share.isActive())
			.build();
	}

}