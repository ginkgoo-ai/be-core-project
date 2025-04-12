package com.ginkgooai.core.project.aspect.lock;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public interface DistributedLockService {
    /**
     * Try to acquire a lock with the given key
     *
     * @param key       The lock key
     * @param waitTime  Maximum time to wait for the lock
     * @param leaseTime Time after which the lock will be automatically released
     * @param unit      Time unit for waitTime and leaseTime
     * @return true if the lock was acquired, false otherwise
     */
    boolean tryLock(String key, long waitTime, long leaseTime, TimeUnit unit);

    /**
     * Release the lock with the given key
     *
     * @param key The lock key
     */
    void unlock(String key);

    /**
     * Execute a task with a distributed lock
     *
     * @param key       The lock key
     * @param waitTime  Maximum time to wait for the lock
     * @param leaseTime Time after which the lock will be automatically released
     * @param unit      Time unit for waitTime and leaseTime
     * @param task      The task to execute
     * @param <T>       The return type of the task
     * @return The result of the task
     * @throws Exception if the task throws an exception
     */
    <T> T executeWithLock(String key, long waitTime, long leaseTime, TimeUnit unit,
                          Supplier<T> task) throws Exception;
}
