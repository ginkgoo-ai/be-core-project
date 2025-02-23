package com.ginkgooai.core.project.dto.request;

import com.ginkgooai.core.project.domain.MemberStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request payload for adding a project member")
public class ProjectMemberRequest {
    @Schema(description = "User ID of the member", example = "user123")
    private String userId;

    @Schema(description = "Member status", example = "ACTIVE")
    private MemberStatus status;

    @Schema(description = "Role ID associated with the member", example = "role123")
    private String roleId;
}