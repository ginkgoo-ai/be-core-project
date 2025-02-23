package com.ginkgooai.core.project.aspect.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RedissonWriteLock {
    /**
     * Spring Expression Language (SpEL) expression for generate the key dynamically.
     */
    String keyGenSpEL() default ""; // Custom lock key (can use SpEL expressions, e.g., "#id" for method parameters)

    /**
     * The duration for which the lock will be held once it's acquired, Time unit: SECONDS
     * <p><strong>Important:</strong> The default value is -1, which enables the Redisson watchdog. Be careful when setting</p>
     * <p>this value, and only do so if you truly need to and understand what the watchdog is.</p>
     */
    long leaseTime() default -1;

    /**
     * Time to wait for the lock, default is -1 which means no waiting, Time unit: SECONDS
     */
    long waitTime() default -1;
}