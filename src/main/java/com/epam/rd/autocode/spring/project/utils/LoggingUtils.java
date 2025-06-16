package com.epam.rd.autocode.spring.project.utils;

import org.aspectj.lang.JoinPoint;
import org.springframework.stereotype.Component;

@Component
public class LoggingUtils {

    public static String formatArguments(Object[] args) {
        if (args == null || args.length == 0) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < args.length; i++) {
            if (i > 0) sb.append(", ");
            Object arg = args[i];
            if (arg == null) sb.append("null");
            else if (containsSensitiveData(arg)) sb.append("[PROTECTED]");
            else sb.append(arg.getClass().getSimpleName()).append("(").append(arg).append(")");
        }
        sb.append("]");
        return sb.toString();
    }

    public static String getClassName(JoinPoint joinPoint) {
        return joinPoint.getTarget().getClass().getSimpleName();
    }

    public static String getMethodName(JoinPoint joinPoint) {
        return joinPoint.getSignature().getName();
    }

    public static String getMethodSignature(JoinPoint joinPoint) {
        return joinPoint.getSignature().toShortString();
    }

    public static String formatException(Throwable exception) {
        return String.format("%s - %s", 
                exception.getClass().getSimpleName(), 
                exception.getMessage());
    }

    public static boolean shouldLogStackTrace(Throwable exception) {
        return !(exception instanceof RuntimeException) ||
               exception.getClass().getPackageName().contains("java.lang");
    }

    private static boolean containsSensitiveData(Object arg) {
        String className = arg.getClass().getSimpleName().toLowerCase();
        String stringValue = arg.toString().toLowerCase();
        
        return className.contains("password") || 
               stringValue.contains("password") ||
               className.contains("token") ||
               stringValue.contains("token");
    }

    public static String formatPerformanceStats(String methodSignature, long calls, 
                                               long avgTime, long minTime, long maxTime) {
        return String.format("Performance stats for %s: calls=%d, avg=%dms, min=%dms, max=%dms", 
                methodSignature, calls, avgTime, minTime, maxTime);
    }
}