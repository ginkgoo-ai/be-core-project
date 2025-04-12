package com.ginkgooai.core.project.service.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailRateLimitService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String RATE_LIMIT_KEY_PREFIX = "email:rate_limit:";
    private static final long RATE_LIMIT_WINDOW = 60; // 1 minute in seconds

    public boolean isRateLimited(String recipientEmail, String emailType) {
        String key = RATE_LIMIT_KEY_PREFIX + recipientEmail + ":" + emailType;
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }

    public void setRateLimit(String recipientEmail, String emailType) {
        String key = RATE_LIMIT_KEY_PREFIX + recipientEmail + ":" + emailType;
        redisTemplate.opsForValue().set(key, "1", RATE_LIMIT_WINDOW, TimeUnit.SECONDS);
    }
}
