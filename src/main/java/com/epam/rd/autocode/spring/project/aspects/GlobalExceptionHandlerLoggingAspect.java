package com.epam.rd.autocode.spring.project.aspects;

import com.epam.rd.autocode.spring.project.dto.ErrorResponseDTO;
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
public class GlobalExceptionHandlerLoggingAspect {

    @Pointcut("execution(* com.epam.rd.autocode.spring.project.controller.GlobalExceptionHandler.*(..))")
    public void globalExceptionHandlerMethod() {}

    @Around("globalExceptionHandlerMethod()")
    public Object logGlobalExceptionHandling(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = LoggingUtils.getMethodName(joinPoint);
        Object[] args = joinPoint.getArgs();
        Exception exception = args.length > 0 && args[0] instanceof Exception ? (Exception) args[0] : null;
        logExceptionHandling(methodName, exception);
        try {
            Object result = joinPoint.proceed();
            logExceptionResponse(methodName, exception, result);
            return result;
        } catch (Exception e) {
            log.error("EXCEPTION_HANDLER_ERROR: {} failed while handling {}: {}",
                methodName, 
                exception != null ? exception.getClass().getSimpleName() : "unknown", 
                LoggingUtils.formatException(e));
            throw e;
        }
    }

    private void logExceptionHandling(String handlerMethod, Exception exception) {
        HttpServletRequest request = getCurrentRequest();
        String requestInfo = "";
        
        if (request != null) {
            requestInfo = String.format(" [%s %s]", request.getMethod(), request.getRequestURI());
        }
        
        if (exception != null) {
            log.warn("EXCEPTION_HANDLED: {} handling {}{} -> {}", 
                handlerMethod,
                exception.getClass().getSimpleName(),
                requestInfo,
                exception.getMessage());
        } else {
            log.warn("EXCEPTION_HANDLED: {} handling unknown exception{}", 
                handlerMethod, 
                requestInfo);
        }
    }

    private void logExceptionResponse(String handlerMethod, Exception exception, Object result) {
        if (result instanceof ResponseEntity<?> responseEntity) {
            String errorMessage = extractErrorMessage(responseEntity.getBody());
            
            log.info("EXCEPTION_RESPONSE: {} -> {} {}", 
                handlerMethod,
                responseEntity.getStatusCode(),
                errorMessage != null ? "\"" + errorMessage + "\"" : "");
        } else {
            log.info("EXCEPTION_RESPONSE: {} -> {}", 
                handlerMethod,
                result != null ? result.getClass().getSimpleName() : "null");
        }
    }

    private String extractErrorMessage(Object responseBody) {
        if (responseBody instanceof ErrorResponseDTO errorResponse) {
            return errorResponse.message();
        }
        return responseBody != null ? responseBody.toString() : null;
    }

    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }
}