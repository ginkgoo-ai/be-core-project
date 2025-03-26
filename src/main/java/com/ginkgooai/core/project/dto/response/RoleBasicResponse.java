// RoleBasicResponse.java
package com.ginkgooai.core.project.dto.response;

import com.ginkgooai.core.project.domain.role.ProjectRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Basic role information response")
public class RoleBasicResponse {
    @Schema(description = "Role ID")
    private String id;
    
    @Schema(description = "Role name")
    private String name;
    
    @Schema(description = "Project ID that role belongs to")
    private String projectId;
    
    public static RoleBasicResponse from(ProjectRole role) {
        return RoleBasicResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .projectId(role.getProject().getId())
                .build();
    }
}