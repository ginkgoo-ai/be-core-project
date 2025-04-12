package com.ginkgooai.core.project.aspect.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisDistributedLockService implements DistributedLockService {

    private final RedissonClient redissonClient;
    private static final String LOCK_KEY_PREFIX = "lock:";

    @Override
    public boolean tryLock(String key, long waitTime, long leaseTime, TimeUnit unit) {
        String lockKey = LOCK_KEY_PREFIX + key;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            return lock.tryLock(waitTime, leaseTime, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Failed to acquire lock for key: {}", key, e);
            return false;
        }
    }

    @Override
    public void unlock(String key) {
        String lockKey = LOCK_KEY_PREFIX + key;
        RLock lock = redissonClient.getLock(lockKey);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }

    @Override
    public <T> T executeWithLock(String key, long waitTime, long leaseTime, TimeUnit unit,
                                 Supplier<T> task) throws Exception {
        if (tryLock(key, waitTime, leaseTime, unit)) {
            try {
                return task.get();
            } finally {
                unlock(key);
            }
        } else {
            throw new IllegalStateException("Failed to acquire lock for key: " + key);
        }
    }
}
