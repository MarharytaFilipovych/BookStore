package com.epam.rd.autocode.spring.project.filters;

import com.epam.rd.autocode.spring.project.model.enums.Role;
import com.epam.rd.autocode.spring.project.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.io.IOException;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private String token;
    private String username;
    private List<String> roles;

    @BeforeEach
    void setUp() {
        token = "valid.jwt.token";
        username = "test@example.com";
        roles = List.of(Role.CLIENT.toString());
        SecurityContextHolder.clearContext();
    }

    @ParameterizedTest
    @CsvSource({
            "CLIENT, valid.jwt.token, test@example.com",
            "EMPLOYEE, employee.jwt.token, employee@example.com"
    })
    void doFilterInternal_WithValidTokens_ShouldSetAuthentication(String roleType, String token, String username)
            throws ServletException, IOException {
        // Arrange
        List<String> roles = List.of("ROLE_" + roleType);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtils.getUserName(token)).thenReturn(username);
        when(jwtUtils.getRoles(token)).thenReturn(roles);
        when(jwtUtils.isTokenExpired(token)).thenReturn(false);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(username, authentication.getPrincipal());
        assertEquals(1, authentication.getAuthorities().size());
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + roleType)));
        verify(filterChain).doFilter(request, response);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "InvalidFormat valid.jwt.token",
            "Basic dXNlcjpwYXNz",
            "ApiKey abc123",
            "Digest username=test",
            "OAuth token123"
    })
    void doFilterInternal_WithInvalidAuthorizationHeaders_ShouldNotSetAuthentication(String authHeader)
            throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(authHeader);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);
        verify(filterChain).doFilter(request, response);
        verify(jwtUtils, never()).getUserName(anyString());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"Bearer ", "Bearer  ", "Bearer\t"})
    void doFilterInternal_WithMissingOrEmptyTokens_ShouldNotSetAuthentication(String authHeader)
            throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(authHeader);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);
        verify(filterChain).doFilter(request, response);
        verify(jwtUtils, never()).getUserName(anyString());
    }

    @Test
    void doFilterInternal_WithExpiredToken_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtils.getUserName(token)).thenReturn(username);
        when(jwtUtils.getRoles(token)).thenReturn(roles);
        when(jwtUtils.isTokenExpired(token)).thenReturn(true);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithInvalidToken_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtils.getUserName(token)).thenReturn(null);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);
        verify(filterChain).doFilter(request, response);
        verify(jwtUtils, never()).getRoles(anyString());
    }

    @Test
    void doFilterInternal_WithExistingAuthentication_ShouldNotOverrideAuthentication() throws ServletException, IOException {
        // Arrange
        Authentication existingAuth = new UsernamePasswordAuthenticationToken(
                "existing@example.com", null, List.of(new SimpleGrantedAuthority(Role.CLIENT.toString()))
        );
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertEquals(existingAuth, authentication);
        assertEquals("existing@example.com", authentication.getPrincipal());
        verify(filterChain).doFilter(request, response);

        verify(jwtUtils, never()).getUserName(anyString());
        verify(jwtUtils, never()).getRoles(anyString());
        verify(jwtUtils, never()).isTokenExpired(anyString());
    }

    @ParameterizedTest
    @ValueSource(classes = {
            RuntimeException.class,
            IllegalArgumentException.class,
            SecurityException.class
    })
    void doFilterInternal_WithTokenProcessingExceptions_ShouldContinueFilterChain(Class<? extends RuntimeException> exceptionClass)
            throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtils.getUserName(token)).thenThrow(exceptionClass);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithEmptyRolesList_ShouldSetAuthenticationWithEmptyAuthorities() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtils.getUserName(token)).thenReturn(username);
        when(jwtUtils.getRoles(token)).thenReturn(List.of());
        when(jwtUtils.isTokenExpired(token)).thenReturn(false);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(username, authentication.getPrincipal());
        assertTrue(authentication.getAuthorities().isEmpty());
        verify(filterChain).doFilter(request, response);
    }

    @ParameterizedTest
    @CsvSource({
            "valid.token, ''",
            "another.token, ' '",
            "test.token, '  '"
    })
    void doFilterInternal_WithInvalidUsernames_ShouldNotSetAuthentication(String token, String username)
            throws ServletException, IOException {
        // Arrange
        String actualUsername = username.trim().isEmpty() ? null : username.trim();
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtils.getUserName(token)).thenReturn(actualUsername);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);
        verify(filterChain).doFilter(request, response);
    }
}