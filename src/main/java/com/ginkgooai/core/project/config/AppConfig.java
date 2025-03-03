package com.ginkgooai.core.project.config;

import com.ginkgooai.core.common.filter.WorkspaceContextFilter;
import com.ginkgooai.core.common.utils.ActivityLogger;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    
    @Bean
    public ActivityLogger activityLogger(RedissonClient redissonClient) {
        return new ActivityLogger(redissonClient);
    }

    @Bean
    public WorkspaceContextFilter workspaceContextFilter() {
        return new WorkspaceContextFilter();
    }
}
