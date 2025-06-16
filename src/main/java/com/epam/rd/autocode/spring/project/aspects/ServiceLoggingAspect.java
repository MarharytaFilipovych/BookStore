package com.epam.rd.autocode.spring.project.aspects;

import com.epam.rd.autocode.spring.project.utils.LoggingUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Aspect
@Component
@Slf4j
public class ServiceLoggingAspect {

    private final ConcurrentHashMap<String, AtomicLong> executionCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> totalExecutionTimes = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> maxExecutionTimes = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> minExecutionTimes = new ConcurrentHashMap<>();

    @Pointcut("execution(* com.epam.rd.autocode.spring.project.service.impl.*.*(..))")
    public void serviceMethod() {}

    @Pointcut("execution(* com.epam.rd.autocode.spring.project.service.impl.*.getAll*(..)) || " +
              "execution(* com.epam.rd.autocode.spring.project.service.impl.*.findAll*(..)) || " +
              "execution(* com.epam.rd.autocode.spring.project.service.impl.*.search*(..))")
    public void potentiallySlowOperations() {}

    @Around("serviceMethod()")
    public Object logServiceMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = LoggingUtils.getClassName(joinPoint);
        String methodName = LoggingUtils.getMethodName(joinPoint);
        String methodSignature = LoggingUtils.getMethodSignature(joinPoint);
        Object[] args = joinPoint.getArgs();
        
        long startTime = System.currentTimeMillis();
        logMethodEntry(className, methodName, args);
        
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            updatePerformanceStatistics(methodSignature, executionTime);
            logPerformanceStatistics(methodSignature);
            logMethodSuccess(className, methodName, executionTime);
            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logMethodException(className, methodName, executionTime, e);
            throw e;
        }
    }

    @AfterThrowing(pointcut = "serviceMethod()", throwing = "exception")
    public void logServiceException(JoinPoint joinPoint, Throwable exception) {
        String className = LoggingUtils.getClassName(joinPoint);
        String methodName = LoggingUtils.getMethodName(joinPoint);
        Object[] args = joinPoint.getArgs();
        
        log.error("Exception occurred in {}.{}() with arguments: {} - Exception: {}", 
                 className, methodName, LoggingUtils.formatArguments(args), 
                 LoggingUtils.formatException(exception));
                 
        if (LoggingUtils.shouldLogStackTrace(exception)) log.error("Stack trace:", exception);
    }


    private void logMethodEntry(String className, String methodName, Object[] args) {
        if (args.length > 0) {
            log.info("Entering {}.{}() with arguments: {}", 
                    className, methodName, LoggingUtils.formatArguments(args));
        } else log.info("Entering {}.{}() with no arguments", className, methodName);
    }

    private void logMethodSuccess(String className, String methodName, long executionTime) {
        log.info("Completed {}.{}() in {}ms", className, methodName, executionTime);
    }

    private void logMethodException(String className, String methodName, long executionTime, Exception e) {
        log.error("Exception in {}.{}() after {}ms: {}", 
                 className, methodName, executionTime, LoggingUtils.formatException(e));
    }

    private void updatePerformanceStatistics(String methodSignature, long executionTime) {
        executionCounts.computeIfAbsent(methodSignature, k -> new AtomicLong(0)).incrementAndGet();
        totalExecutionTimes.computeIfAbsent(methodSignature, k -> new AtomicLong(0)).addAndGet(executionTime);
        maxExecutionTimes.merge(methodSignature, executionTime, Long::max);
        minExecutionTimes.merge(methodSignature, executionTime, Long::min);
    }

    private void logPerformanceStatistics(String methodSignature) {
        AtomicLong count = executionCounts.get(methodSignature);
        AtomicLong totalTime = totalExecutionTimes.get(methodSignature);
        Long maxTime = maxExecutionTimes.get(methodSignature);
        Long minTime = minExecutionTimes.get(methodSignature);
        
        if (count != null && totalTime != null) {
            long avgTime = totalTime.get() / count.get();
            log.info(LoggingUtils.formatPerformanceStats(methodSignature, count.get(), avgTime, minTime, maxTime));
        }
    }
}