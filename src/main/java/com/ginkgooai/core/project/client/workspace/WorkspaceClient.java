package com.ginkgooai.core.project.client.workspace;

import com.ginkgooai.core.common.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "workspace-service", url="${core-workspace-uri}", configuration = FeignConfig.class)
public interface WorkspaceClient {
    
    @GetMapping("/workspaces/{workspaceId}/validate")
    boolean validateWorkspaceAccess(@PathVariable("workspaceId") String workspaceId);
}