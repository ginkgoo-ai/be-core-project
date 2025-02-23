package com.ginkgooai.core.project.dto.response;

import com.ginkgooai.core.project.domain.MemberStatus;
import com.ginkgooai.core.project.domain.ProjectMember;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Response payload for a project member")
public class ProjectMemberResponse {
    @Schema(description = "ID of the member", example = "member123")
    private String id;

    @Schema(description = "User ID of the member", example = "user123")
    private String userId;

    @Schema(description = "Member status", example = "ACTIVE")
    private MemberStatus status;

    @Schema(description = "Project ID associated with the member", example = "proj123")
    private String projectId;

    public static ProjectMemberResponse mapToProjectMemberResponse(ProjectMember member) {
        ProjectMemberResponse response = new ProjectMemberResponse();
        response.setId(member.getId());
        response.setUserId(member.getUserId());
        response.setStatus(member.getStatus());
        response.setProjectId(member.getProject().getId());
        return response;
    }
}