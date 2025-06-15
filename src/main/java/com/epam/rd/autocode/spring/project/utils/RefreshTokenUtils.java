package com.epam.rd.autocode.spring.project.utils;

import java.security.SecureRandom;
import java.util.Base64;

public class RefreshTokenUtils {
    private static final SecureRandom secureRandom = new SecureRandom(); // threadsafe
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder().withoutPadding();

    public static String generateRefreshToken() {
        byte[] randomBytes = new byte[64];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }
}
