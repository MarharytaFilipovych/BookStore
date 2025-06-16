package com.epam.rd.autocode.spring.project.aspects;

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
public class LoggingAspect {

    private final ConcurrentHashMap<String, AtomicLong> executionCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> totalExecutionTimes = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> maxExecutionTimes = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> minExecutionTimes = new ConcurrentHashMap<>();

    @Pointcut("execution(* com.epam.rd.autocode.spring.project.service.impl.*.*(..))")
    public void serviceMethod() {}

    @Pointcut("execution(* com.epam.rd.autocode.spring.project.repo.*.*(..))")
    public void repositoryMethod() {}

    @Pointcut("execution(* com.epam.rd.autocode.spring.project.service.impl.*.getAll*(..)) || " +
              "execution(* com.epam.rd.autocode.spring.project.service.impl.*.findAll*(..)) || " +
              "execution(* com.epam.rd.autocode.spring.project.service.impl.*.search*(..))")
    public void potentiallySlowOperations() {}

    @Around("serviceMethod()")
    public Object logServiceMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String methodSignature = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();
        
        long startTime = System.currentTimeMillis();
        
        if (args.length > 0) log.info("Entering {}.{}() with arguments: {}", className, methodName, formatArguments(args));
        else log.info("Entering {}.{}() with no arguments", className, methodName);
        
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            updateStatistics(methodSignature, executionTime);
            logMethodStatistics(methodSignature);
            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("Exception in {}.{}() after {}ms: {} - {}",
                     className, methodName, executionTime, 
                     e.getClass().getSimpleName(), e.getMessage());
            throw e;
        }
    }

    @Before("repositoryMethod()")
    public void logRepositoryMethodCall(JoinPoint joinPoint) {
        if (log.isDebugEnabled()) {
            String className = joinPoint.getTarget().getClass().getSimpleName();
            String methodName = joinPoint.getSignature().getName();
            Object[] args = joinPoint.getArgs();
            if (args.length > 0) log.debug("Calling repository method {}.{}() with arguments: {}", className, methodName, formatArguments(args));
            else log.debug("Calling repository method {}.{}() with no arguments", className, methodName);
        }
    }

    @AfterThrowing(pointcut = "serviceMethod()", throwing = "exception")
    public void logServiceException(JoinPoint joinPoint, Throwable exception) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        
        log.error("Exception occurred in {}.{}() with arguments: {} - Exception: {} - Message: {}", 
                 className, methodName, formatArguments(args), 
                 exception.getClass().getSimpleName(), exception.getMessage());
        if (!(exception instanceof RuntimeException) ||
            exception.getClass().getPackageName().contains("java.lang")) {
            log.error("Stack trace:", exception);
        }
    }

    private void updateStatistics(String methodSignature, long executionTime) {
        executionCounts.computeIfAbsent(methodSignature, k -> new AtomicLong(0)).incrementAndGet();
        totalExecutionTimes.computeIfAbsent(methodSignature, k -> new AtomicLong(0)).addAndGet(executionTime);
        maxExecutionTimes.merge(methodSignature, executionTime, Long::max);
        minExecutionTimes.merge(methodSignature, executionTime, Long::min);
    }

    private void logMethodStatistics(String methodSignature) {
        AtomicLong count = executionCounts.get(methodSignature);
        AtomicLong totalTime = totalExecutionTimes.get(methodSignature);
        Long maxTime = maxExecutionTimes.get(methodSignature);
        Long minTime = minExecutionTimes.get(methodSignature);
        
        if (count != null && totalTime != null) {
            long avgTime = totalTime.get() / count.get();
            log.info("Performance stats for {}: calls={}, avg={}ms, min={}ms, max={}ms", 
                    methodSignature, count.get(), avgTime, minTime, maxTime);
        }
    }

    private String formatArguments(Object[] args) {
        if (args == null || args.length == 0) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < args.length; i++) {
            if (i > 0) sb.append(", ");
            Object arg = args[i];
            if (arg == null) sb.append("null");
            else if (arg.getClass().getSimpleName().toLowerCase().contains("password") ||
                    arg.toString().toLowerCase().contains("password")) sb.append("[PROTECTED]");
            else sb.append(arg.getClass().getSimpleName()).append("(").append(arg).append(")");
        }
        sb.append("]");
        return sb.toString();
    }
}