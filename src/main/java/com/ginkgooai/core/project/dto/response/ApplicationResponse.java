package com.ginkgooai.core.project.dto.response;

import com.ginkgooai.core.common.constant.ContextsConstant;
import com.ginkgooai.core.common.utils.ContextUtils;
import com.ginkgooai.core.project.client.identity.dto.UserInfoResponse;
import com.ginkgooai.core.project.client.storage.dto.CloudFileResponse;
import com.ginkgooai.core.project.domain.application.Application;
import com.ginkgooai.core.project.domain.application.ApplicationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Builder
@Slf4j
@Schema(description = "Response object containing application details")
public class ApplicationResponse {

    @Schema(description = "Unique identifier of the application", example = "app_12345")
    private String id;

    @Schema(description = "Workspace identifier", example = "ws_12345")
    private String workspaceId;

    @Schema(description = "Project identifier", example = "proj_12345")
    private String projectId;

    @Schema(description = "Project name", example = "my project")
    private String projectName;

	@Schema(description = "Project Description", example = "This is my project")
	private String projectDescription;

	@Schema(description = "Project Plot line", example = "Story...")
	private String projectPlotLine;

    @Schema(description = "Role")
    private ProjectRoleResponse role;

    @Schema(description = "Associated talent details")
    private TalentResponse talent;

    @Schema(description = "List of submissions")
    private List<SubmissionResponse> submissions;

    @Schema(description = "Current application status", example = "PENDING")
    private ApplicationStatus status;

    @Schema(description = "List of application notes")
    private List<ApplicationNoteResponse> notes;

    @Schema(description = "List of application comments")
    private List<ApplicationCommentResponse> comments;

    @Schema(description = "User who created the application", example = "user_12345")
    private String createdBy;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

	public static ApplicationResponse from(Application application, List<UserInfoResponse> users, String userId,
			Map<String, CloudFileResponse> roleSideFilesMap) {
        List<String> role = ContextUtils.get().get(ContextsConstant.USER_ROLE, List.class);
        boolean isTalentRole = role.size() == 1 && role.get(0).equals("ROLE_TALENT");

        Map<String, UserInfoResponse> userInfoMap = users.stream()
                .collect(Collectors.toMap(UserInfoResponse::getId, user -> user));

        ApplicationResponse response = ApplicationResponse.builder()
                .id(application.getId())
                .workspaceId(application.getWorkspaceId())
                .projectId(application.getProject().getId())
			.projectName(application.getProject().getName())
			.projectDescription(application.getProject().getDescription())
			.projectPlotLine(application.getProject().getPlotLine())
			.role(ProjectRoleResponse.from(application.getRole(), roleSideFilesMap))
                .talent(TalentResponse.from(application.getTalent()))
			.submissions(ObjectUtils.isEmpty(application.getSubmissions()) ? null
					: application.getSubmissions()
						.stream()
						.map(submission -> SubmissionResponse.from(submission, users, userId))
						.toList())
                .status(application.getStatus())
			.notes(isTalentRole ? null
					: application.getNotes()
						.stream()
						.map(note -> ApplicationNoteResponse.from(note, userInfoMap.get(note.getCreatedBy())))
						.sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
						.toList())
			.comments(isTalentRole ? null : application.getComments()
				.stream()
				.map(comment -> ApplicationCommentResponse.from(comment, userInfoMap.get(comment.getCreatedBy())))
				.sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
				.toList())
                .createdBy(application.getCreatedBy())
                .createdAt(application.getCreatedAt())
                .updatedAt(application.getUpdatedAt())
                .build();

        return response;
    }

    public static ApplicationResponse from(Application application) {
		return from(application, Collections.emptyList(), ContextUtils.getUserId(), Collections.emptyMap());
	}

	public static ApplicationResponse from(Application application, List<UserInfoResponse> users, String userId) {
		return from(application, users, userId, Collections.emptyMap());
    }

}
