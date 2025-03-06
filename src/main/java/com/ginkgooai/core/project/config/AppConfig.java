package com.ginkgooai.core.project.config;

import com.ginkgooai.core.common.utils.ActivityLogger;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    
    @Bean
    public ActivityLogger activityLogger(RedissonClient redissonClient) {
        return new ActivityLogger(redissonClient);
    }

}
