package com.ginkgooai.core.project.aspect.lock;


import com.ginkgooai.core.project.aspect.lock.annotation.DistributedLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class DistributedLockAspect {

    private final DistributedLockService distributedLockService;
    private final ExpressionParser expressionParser = new SpelExpressionParser();

    @Around("@within(distributedLock) || @annotation(distributedLock)")
    public Object around(ProceedingJoinPoint joinPoint, DistributedLock distributedLock)
        throws Throwable {
        // Generate lock key
        String key = evaluateKeyExpression(joinPoint, distributedLock.key());

        // Try to acquire the lock
        if (!distributedLockService.tryLock(key, distributedLock.waitTime(),
            distributedLock.leaseTime(), distributedLock.unit())) {
            throw new IllegalStateException("Failed to acquire lock for key: " + key);
        }

        // Create a flag to track if the lock should be released
        AtomicBoolean lockAcquired = new AtomicBoolean(true);

        try {
            // Register a transaction synchronization if we're in a transaction
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager
                    .registerSynchronization(new TransactionSynchronization() {
                        @Override
                        public void afterCompletion(int status) {
                            if (lockAcquired.get()) {
                                distributedLockService.unlock(key);
                                lockAcquired.set(false);
                            }
                        }
                    });
            }

            // Execute the method
            return joinPoint.proceed();
        } catch (Throwable e) {
            // If an exception occurs and we're not in a transaction, release the lock
            if (!TransactionSynchronizationManager.isSynchronizationActive()
                && lockAcquired.get()) {
                distributedLockService.unlock(key);
                lockAcquired.set(false);
            }
            throw e;
        } finally {
            // If we're not in a transaction and the lock hasn't been released yet, release it
            if (!TransactionSynchronizationManager.isSynchronizationActive()
                && lockAcquired.get()) {
                distributedLockService.unlock(key);
            }
        }
    }


    private String evaluateKeyExpression(ProceedingJoinPoint joinPoint, String keyExpression) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();
        String[] parameterNames = signature.getParameterNames();

        EvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }

        Expression expression = expressionParser.parseExpression(keyExpression);
        return expression.getValue(context, String.class);
    }
}
