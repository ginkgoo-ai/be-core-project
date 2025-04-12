package com.ginkgooai.core.project.aspect.lock.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {
    /**
     * The key expression for the lock
     * Supports SpEL expressions
     * For class level, use #this to refer to the current object
     * For method level, use method parameters
     */
    String key() default "";

    /**
     * Maximum time to wait for the lock
     */
    long waitTime() default 30;

    /**
     * Time after which the lock will be automatically released
     */
    long leaseTime() default 30;

    /**
     * Time unit for waitTime and leaseTime
     */
    TimeUnit unit() default TimeUnit.SECONDS;

}