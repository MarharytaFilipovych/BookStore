package com.epam.rd.autocode.spring.project.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter extends OncePerRequestFilter{

    private final Map<String, RequestInfo> requestCounts = new ConcurrentHashMap<>();

    @Value("${rate.max-requests}")
    private int maxRequests;

    @Value("${rate.time-window-minutes}")
    private int timeWindowMinutes;
    
    private static class RequestInfo {
        int count;
        long firstRequestTime;

        RequestInfo(int count, long firstRequestTime) {
            this.count = count;
            this.firstRequestTime = firstRequestTime;
        }
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (!request.getRequestURI().contains("/login")) {
            filterChain.doFilter(request, response);
            return;
        }
        String clientIp = request.getRemoteAddr();
        long currentTime = System.currentTimeMillis();

        long timeWindow = timeWindowMinutes * 60 * 1000L;
        RequestInfo requestInfo = requestCounts.get(clientIp);

        if(requestInfo  == null || (currentTime - requestInfo.firstRequestTime) >= timeWindow) {
            requestInfo = new RequestInfo(1, currentTime);
            requestCounts.put(clientIp, requestInfo);
            filterChain.doFilter(request, response);
            return;
        }

        if(requestInfo.count >= maxRequests){
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Too many requests!!!");
            return;
        }

        requestInfo.count++;
        filterChain.doFilter(request, response);
    }
}
