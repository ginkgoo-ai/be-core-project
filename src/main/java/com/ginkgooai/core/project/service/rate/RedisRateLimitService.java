package com.ginkgooai.core.project.service.rate;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisRateLimitService implements RateLimitService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String RATE_LIMIT_KEY_PREFIX = "rate_limit:";
    private static final long DEFAULT_EXPIRATION_SECONDS = 60;

    @Override
    public boolean isRateLimited(String key) {
        String fullKey = RATE_LIMIT_KEY_PREFIX + key;
        return Boolean.TRUE.equals(redisTemplate.hasKey(fullKey));
    }

    @Override
    public void setRateLimit(String key) {
        String fullKey = RATE_LIMIT_KEY_PREFIX + key;
        redisTemplate.opsForValue().set(fullKey, "1", DEFAULT_EXPIRATION_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    public long getRemainingTime(String key) {
        String fullKey = RATE_LIMIT_KEY_PREFIX + key;
        return redisTemplate.getExpire(fullKey, TimeUnit.SECONDS);
    }
}
