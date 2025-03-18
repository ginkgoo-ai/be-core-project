package com.ginkgooai.core.project.dto.response;

import com.ginkgooai.core.project.domain.role.ProjectRole;
import com.ginkgooai.core.project.domain.role.RoleStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Project role dropdown option response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Project role dropdown option")
public class ProjectRoleOptionResponse {

    @Schema(description = "Role ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private String id;

    @Schema(description = "Role name", example = "Lead Character")
    private String name;

    @Schema(description = "Role status", example = "CASTING")
    private RoleStatus status;
    
    /**
     * Convert from ProjectRole entity to DTO
     * 
     * @param role The ProjectRole entity
     * @return ProjectRoleOptionResponse DTO
     */
    public static ProjectRoleOptionResponse from(ProjectRole role) {
        return ProjectRoleOptionResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .status(role.getStatus())
                .build();
    }
}