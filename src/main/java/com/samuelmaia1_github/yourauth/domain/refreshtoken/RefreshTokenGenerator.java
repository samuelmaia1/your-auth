package com.samuelmaia1_github.yourauth.domain.refreshtoken;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
public class RefreshTokenGenerator {
    private static final int TOKEN_SIZE = 32;

    private final SecureRandom secureRandom;

    public RefreshTokenGenerator() {
        this.secureRandom = new SecureRandom();
    }

    public String generate() {
        byte[] randomBytes = new byte[TOKEN_SIZE];
        secureRandom.nextBytes(randomBytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
    }
}
