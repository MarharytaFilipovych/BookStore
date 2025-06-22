package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.request.ForgotPasswordDTO;
import com.epam.rd.autocode.spring.project.dto.request.LoginDTO;
import com.epam.rd.autocode.spring.project.dto.request.LogoutDTO;
import com.epam.rd.autocode.spring.project.dto.request.ResetPasswordDto;
import com.epam.rd.autocode.spring.project.dto.request.RefreshTokenDTO;
import com.epam.rd.autocode.spring.project.dto.TokenResponseDTO;
import org.springframework.security.core.Authentication;

import java.util.UUID;

public interface AuthService {
    TokenResponseDTO loginUser(LoginDTO request);

    TokenResponseDTO refreshToken(RefreshTokenDTO request);

    UUID forgotPassword(ForgotPasswordDTO request);

    void changePassword(ResetPasswordDto request);

    void logout(LogoutDTO request);
}
