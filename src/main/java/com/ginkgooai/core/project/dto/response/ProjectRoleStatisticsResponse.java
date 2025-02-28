package com.ginkgooai.core.project.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectRoleStatisticsResponse {
    
    @Schema(description = "Total number of talents for this role")
    private Integer total = 0;
    
    @Schema(description = "Number of talents added")
    private Integer added = 0;
    
    @Schema(description = "Number of talents who submitted")
    private Integer submitted = 0;
    
    @Schema(description = "Number of talents shortlisted")
    private Integer shortlisted = 0;
    
    @Schema(description = "Number of talents declined")
    private Integer declined = 0;
}