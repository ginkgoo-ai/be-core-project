package com.ginkgooai.core.project.dto.response;

import com.ginkgooai.core.project.domain.project.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Response payload for a project")
public class ProjectListResponse {

	@Schema(description = "ID of the project", example = "proj123")
	private String id;

	@Schema(description = "Name of the project", example = "Summer Feature 2025")
	private String name;

	@Schema(description = "Status of the project", example = "IN_PROGRESS")
	private ProjectStatus status;

	@Schema(description = "Number of roles in the project", example = "5")
	private long roleCount;

	@Schema(description = "Number of pending review submissions", example = "8")
	private long pendingReviewCount;

	@Schema(description = "Timestamp when the project was last updated", example = "2025-03-25T10:30:00")
	private LocalDateTime updatedAt;

	@Schema(description = "Project poster Url", example = "www.example.com/poster.jpg")
	private String posterUrl;

	@Schema(description = "Producer of the project", example = "Mark Ronson")
	private String producer;

}