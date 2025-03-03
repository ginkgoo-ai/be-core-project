package com.ginkgooai.core.project.dto.request;

import lombok.Data;

import java.util.Set;

@Data
public class TalentSearchRequest {
    
    private String workspaceId;
    
    private String keyword;
    
    private String agencyName;
    
    private String agentName;
}