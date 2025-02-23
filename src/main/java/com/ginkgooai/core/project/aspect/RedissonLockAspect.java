package com.ginkgooai.core.project.aspect;

import com.ginkgooai.core.common.exception.InternalServiceException;
import com.ginkgooai.core.project.aspect.annotation.RedissonWriteLock;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
@Slf4j
public class RedissonLockAspect {

    @Autowired
    private RedissonClient redissonClient;

    private static final String LOCK_PREFIX = "project:lock:";
    private static final long DEFAULT_WAIT_TIME = 10; // Default wait time in seconds
    private static final long DEFAULT_LEASE_TIME = 30; // Default lease time in seconds

    private final ExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(redissonWriteLock)")
    public Object applyRedissonLock(ProceedingJoinPoint joinPoint, RedissonWriteLock redissonWriteLock) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        String keyGen = redissonWriteLock.keyGenSpEL();
        String key = expressEval(joinPoint, keyGen, String.class);
        if (ObjectUtils.isEmpty(key)) {
            return joinPoint.proceed();
        }

        RLock fairLock = redissonClient.getFairLock(key);
        try {
            if (fairLock.tryLock(DEFAULT_WAIT_TIME, TimeUnit.SECONDS)) {
                return joinPoint.proceed();
            } else {
                throw new InternalServiceException("Failed to acquire lock for method: " + method.getName());
            }
        } finally {
            if (fairLock.isLocked() && fairLock.isHeldByCurrentThread()) {
                fairLock.unlock();
            }
        }

    }

    private <T> T expressEval(ProceedingJoinPoint joinPoint, String express, Class<T> clazz) {
        try {
            MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
            String[] parameterNames = methodSignature.getParameterNames();
            Object[] args = joinPoint.getArgs();

            ExpressionParser parser = new SpelExpressionParser();
            Expression exp = parser.parseExpression(express);
            EvaluationContext context = new StandardEvaluationContext();

            for (int i = 0; i < args.length; i++) {
                context.setVariable(parameterNames[i], args[i]);
            }

            return exp.getValue(context, clazz);
        } catch (Exception e) {
            log.error("express parse error, express={}", express, e);
        }

        return null;
    }
}
