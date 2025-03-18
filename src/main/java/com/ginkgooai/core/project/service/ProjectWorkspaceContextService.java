package com.ginkgooai.core.project.service;

import com.ginkgooai.core.common.constant.RedisKey;
import com.ginkgooai.core.project.client.workspace.WorkspaceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectWorkspaceContextService {
    private final RedisTemplate<String, String> redisTemplate;
    private final WorkspaceClient workspaceClient;  // FeignClient
    
    // Same constants as WorkspaceContextService
    private static final long EXPIRATION = 24 * 60 * 60;
    private static final long REFRESH_THRESHOLD = 6 * 60 * 60;
    
    /**
     * Validates if a user has access to a workspace
     */
    public boolean validateUserWorkspaceAccess(String userId, String workspaceId) {
        // If no workspace ID, reject immediately
        if (workspaceId == null || workspaceId.isEmpty()) {
            return false;
        }

        // First check local cache
        String cacheKey = RedisKey.WORKSPACE_CONTEXT_KEY_PREFIX + userId;
        String cacheWorkspaceIds = redisTemplate.opsForValue().get(cacheKey);
        
        if (!ObjectUtils.isEmpty(cacheWorkspaceIds) ) {
            return Arrays.asList(cacheWorkspaceIds.split(",")).contains(workspaceId);
        }
        
        // Cache miss - call Workspace service API
        try {
            return workspaceClient.validateWorkspaceAccess(workspaceId);
        } catch (Exception e) {
            // Handle service call failure
            log.error("Failed to validate workspace access: {}", e.getMessage());
            return false;
        }
    }
}