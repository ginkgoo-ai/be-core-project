package com.ginkgooai.core.project.dto.response;

import com.ginkgooai.core.project.domain.project.Project;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectBasicResponse {
    private String id;
    private String name;

    public static ProjectBasicResponse fromProject(Project project) {
        ProjectBasicResponse response = new ProjectBasicResponse();
        response.setId(project.getId());
        response.setName(project.getName());
        return response;
    }
}