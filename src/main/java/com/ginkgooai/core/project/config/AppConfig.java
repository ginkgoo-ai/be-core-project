package com.ginkgooai.core.project.config;

import com.ginkgooai.core.common.queue.QueueInterface;
import com.ginkgooai.core.common.queue.RedissonMQ;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    
    @Bean
    public QueueInterface redissonMQ(RedissonClient redissonClient) {
        return new RedissonMQ(redissonClient);
    }

}
