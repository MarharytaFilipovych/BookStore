package com.epam.rd.autocode.spring.project.aspects;

import com.epam.rd.autocode.spring.project.utils.LoggingUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class RepositoryLoggingAspect {

    @Pointcut("execution(* com.epam.rd.autocode.spring.project.repo.*.*(..))")
    public void repositoryMethod() {}

    @Pointcut("execution(* com.epam.rd.autocode.spring.project.repo.*.save*(..)) || " +
              "execution(* com.epam.rd.autocode.spring.project.repo.*.delete*(..)) || " +
              "execution(* com.epam.rd.autocode.spring.project.repo.*.update*(..))")
    public void dataModificationMethods() {}

    @Pointcut("execution(* com.epam.rd.autocode.spring.project.repo.*.find*(..)) || " +
              "execution(* com.epam.rd.autocode.spring.project.repo.*.get*(..)) || " +
              "execution(* com.epam.rd.autocode.spring.project.repo.*.exists*(..))")
    public void dataRetrievalMethods() {}


    @Before("repositoryMethod()")
    public void logRepositoryMethodCall(JoinPoint joinPoint) {
        if (log.isDebugEnabled()) {
            String className = LoggingUtils.getClassName(joinPoint);
            String methodName = LoggingUtils.getMethodName(joinPoint);
            Object[] args = joinPoint.getArgs();
            
            if (args.length > 0) {
                log.debug("Calling repository method {}.{}() with arguments: {}",
                         className, methodName, LoggingUtils.formatArguments(args));
            } else log.debug("Calling repository method {}.{}() with no arguments", className, methodName);
        }
    }

    @Around("dataModificationMethods()")
    public Object logDataModificationOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = LoggingUtils.getClassName(joinPoint);
        String methodName = LoggingUtils.getMethodName(joinPoint);
        Object[] args = joinPoint.getArgs();
        
        long startTime = System.currentTimeMillis();
        log.info("Executing data modification: {}.{}() with arguments: {}", className, methodName, LoggingUtils.formatArguments(args));
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            
            log.info("Data modification completed: {}.{}() in {}ms", className, methodName, executionTime);
            
            if (executionTime > 1000) {
                log.warn("Slow database operation detected: {}.{}() took {}ms", 
                        className, methodName, executionTime);
            }
            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("Data modification failed: {}.{}() after {}ms - {}", 
                     className, methodName, executionTime, LoggingUtils.formatException(e));
            throw e;
        }
    }

    @AfterReturning(pointcut = "dataRetrievalMethods()", returning = "result")
    public void logDataRetrievalResult(JoinPoint joinPoint, Object result) {
        if (log.isDebugEnabled()) {
            String className = LoggingUtils.getClassName(joinPoint);
            String methodName = LoggingUtils.getMethodName(joinPoint);
            String resultInfo = formatRetrievalResult(result);
            log.debug("Data retrieval completed: {}.{}() - {}", className, methodName, resultInfo);
        }
    }

    @AfterThrowing(pointcut = "repositoryMethod()", throwing = "exception")
    public void logRepositoryException(JoinPoint joinPoint, Throwable exception) {
        String className = LoggingUtils.getClassName(joinPoint);
        String methodName = LoggingUtils.getMethodName(joinPoint);
        Object[] args = joinPoint.getArgs();
        log.error("Repository exception in {}.{}() with arguments: {} - {}", 
                 className, methodName, LoggingUtils.formatArguments(args), 
                 LoggingUtils.formatException(exception));
        if (isDatabaseException(exception)) log.error("Database exception stack trace:", exception);
    }

    private String formatRetrievalResult(Object result) {
        if (result == null) return "Result: null";
        if (result instanceof java.util.Collection<?> collection) return String.format("Result: Collection with %d items", collection.size());
        if (result instanceof java.util.Optional<?> optional) return optional.isPresent() ? "Result: Optional with value" : "Result: Empty Optional";
        if (result instanceof Boolean) return String.format("Result: %s", result);
        return String.format("Result: %s", result.getClass().getSimpleName());
    }

    private boolean isDatabaseException(Throwable exception) {
        String className = exception.getClass().getName().toLowerCase();
        return className.contains("sql") || 
               className.contains("database") || 
               className.contains("hibernate") || 
               className.contains("jpa") ||
               className.contains("dataaccess");
    }
}