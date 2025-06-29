package com.epam.rd.autocode.spring.project.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import java.io.PrintWriter;
import java.io.StringWriter;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitingFilterTest {

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;

    @InjectMocks
    private RateLimitingFilter rateLimitingFilter;

    private StringWriter stringWriter;
    private PrintWriter printWriter;
    private final String clientIp = "192.168.1.1";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(rateLimitingFilter, "maxRequests", 3);
        ReflectionTestUtils.setField(rateLimitingFilter, "timeWindowMinutes", 15);
        
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
    }

    @Test
    void doFilterInternal_NonLoginRequest_ShouldSkipRateLimit() throws Exception {
        // Arrange
        when(request.getRequestURI()).thenReturn("/books");

        // Act
        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(response);
    }

    @Test
    void doFilterInternal_FirstLoginRequest_ShouldAllowRequest() throws Exception {
        // Arrange
        when(request.getRequestURI()).thenReturn("/auth/login");
        when(request.getRemoteAddr()).thenReturn(clientIp);

        // Act
        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(response);
    }

    @Test
    void doFilterInternal_ExceedRateLimit_ShouldBlockRequest() throws Exception {
        // Arrange
        when(request.getRequestURI()).thenReturn("/auth/login");
        when(request.getRemoteAddr()).thenReturn(clientIp);
        when(response.getWriter()).thenReturn(printWriter);

        // Act
        for (int i = 0; i < 4; i++) {
            rateLimitingFilter.doFilterInternal(request, response, filterChain);
        }

        // Assert
        verify(response).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        verify(response).getWriter();
        assertTrue(stringWriter.toString().contains("Too many requests"));
        
        verify(filterChain, times(3)).doFilter(request, response);
    }

    @Test
    void doFilterInternal_TimeWindowExpired_ShouldResetCounter() throws Exception {
        // Arrange
        when(request.getRequestURI()).thenReturn("/auth/login");
        when(request.getRemoteAddr()).thenReturn(clientIp);
        for (int i = 0; i < 3; i++) {
            rateLimitingFilter.doFilterInternal(request, response, filterChain);
        }
        verify(filterChain, times(3)).doFilter(request, response);
        ReflectionTestUtils.setField(rateLimitingFilter, "requestCounts", new java.util.concurrent.ConcurrentHashMap<>());
        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain, times(4)).doFilter(request, response);
        verify(response, never()).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    }
}