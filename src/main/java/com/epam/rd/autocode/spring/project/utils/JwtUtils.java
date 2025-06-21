package com.epam.rd.autocode.spring.project.utils;

import com.epam.rd.autocode.spring.project.conf.JwtSettings;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

@Service
public class JwtUtils {
    private final JwtSettings settings;

    public JwtUtils(JwtSettings settings) {
        this.settings = settings;
    }

    public String generateToken(Authentication authentication) {
        return Jwts.builder()
                .issuer(settings.getIssuer())
                .subject((String) authentication.getPrincipal())
                .claim("roles", authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + settings.getExpirationTime().toMillis()))
                .signWith(getSignInKey())
                .compact();
    }

    public String getUserName(String token){
        Claims claims = extractAllClaims(token);
        return claims != null ? claims.getSubject() : null;
    }

    public List<String> getRoles(String token) {
        Claims claims = extractAllClaims(token);
        if (claims == null) {
            return List.of();
        }

        Object rolesObj = claims.get("roles");
        if (rolesObj instanceof List<?>) {
            return ((List<?>) rolesObj).stream()
                    .map(Object::toString)
                    .toList();
        }
        return List.of();
    }

    public boolean isTokenExpired(String token){
        Claims claims = extractAllClaims(token);
        return claims == null || claims.getExpiration().before(new Date());
    }

    public boolean isTokenValid(String token, Authentication authentication) {
        String username = getUserName(token);
        String principal = (String) authentication.getPrincipal();
        return username != null &&
                username.equals(principal) &&
                !isTokenExpired(token);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts
                    .parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    private SecretKey getSignInKey(){
        byte[] keyBytes = Decoders.BASE64.decode(settings.getSecretKey());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}