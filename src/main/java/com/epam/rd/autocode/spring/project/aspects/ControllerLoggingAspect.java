package com.epam.rd.autocode.spring.project.aspects;

import com.epam.rd.autocode.spring.project.utils.LoggingUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Aspect
@Component
@Slf4j
public class ControllerLoggingAspect {

    @Pointcut("execution(* com.epam.rd.autocode.spring.project.controller.*.*(..)) && " +
            "!execution(* com.epam.rd.autocode.spring.project.controller.GlobalExceptionHandler.*(..))")
    public void controllerMethod() {}

    @Around("controllerMethod()")
    public Object logControllerExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = LoggingUtils.getClassName(joinPoint);
        String methodName = LoggingUtils.getMethodName(joinPoint);
        HttpServletRequest request = getCurrentRequest();
        String requestLog = buildRequestLog(request, className, methodName, joinPoint.getArgs());

        try {
            Object result = joinPoint.proceed();
            String responseLog = buildResponseLog(className, methodName, result);
            log.info("{} -> {}", requestLog, responseLog);
            return result;
        } catch (Exception e) {
            log.error("{} -> EXCEPTION: {}", requestLog, LoggingUtils.formatException(e));
            throw e;
        }
    }

    private String buildRequestLog(HttpServletRequest request, String className, String methodName, Object[] args) {
        StringBuilder sb = new StringBuilder();

        if (request != null) {
            sb.append(request.getMethod()).append(" ").append(request.getRequestURI());
        }

        sb.append(" ").append(className).append(".").append(methodName).append("()");

        if (args.length > 0) {
            sb.append(" ").append(LoggingUtils.formatArguments(args));
        }

        return sb.toString();
    }

    private String buildResponseLog(String className, String methodName, Object result) {
        if (result instanceof ResponseEntity<?> responseEntity) {
            return String.format("%s %s",
                    responseEntity.getStatusCode(),
                    getResponseBody(responseEntity.getBody()));
        }
        return result != null ? result.getClass().getSimpleName() : "null";
    }

    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String getResponseBody(Object body) {
        if (body == null) return "null";
        if (body instanceof String || body instanceof Number || body instanceof Boolean) {
            return body.toString();
        }
        return body.getClass().getSimpleName();
    }
}