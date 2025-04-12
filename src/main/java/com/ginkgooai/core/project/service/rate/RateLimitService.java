package com.ginkgooai.core.project.service.rate;

public interface RateLimitService {
    /**
     * Check if the given key is rate limited
     *
     * @param key The key to check
     * @return true if rate limited, false otherwise
     */
    boolean isRateLimited(String key);

    /**
     * Set rate limit for the given key
     *
     * @param key The key to set rate limit for
     */
    void setRateLimit(String key);

    /**
     * Get the remaining time in seconds before the rate limit expires
     *
     * @param key The key to check
     * @return Remaining time in seconds, or 0 if not rate limited
     */
    long getRemainingTime(String key);
}
