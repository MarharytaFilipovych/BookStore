package com.epam.rd.autocode.spring.project.service;

import org.springframework.security.core.userdetails.UserDetails;
import java.util.UUID;

public interface ResetCodeService {
    UUID generateResetCode(UserDetails user);

    boolean isValidResetCode(UUID code, UserDetails user);
}
