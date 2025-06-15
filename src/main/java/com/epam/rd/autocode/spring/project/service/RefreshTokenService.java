package com.epam.rd.autocode.spring.project.service;

import org.springframework.security.core.userdetails.UserDetails;
import java.util.UUID;

public interface RefreshTokenService {
    UUID generateRefreshToken(UserDetails userDetails);

    boolean isValidRefreshToken(UUID token, UserDetails user);
}
