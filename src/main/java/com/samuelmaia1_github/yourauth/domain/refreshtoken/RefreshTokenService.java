package com.samuelmaia1_github.yourauth.domain.refreshtoken;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenHasher hasher;
    private final RefreshTokenRepository repository;
    private final SecureRandom secureRandom;
    private final Integer tokenSize = 32;

    public RefreshTokenService(RefreshTokenHasher hasher, RefreshTokenRepository repository) {
        this.hasher = hasher;
        this.repository = repository;
        this.secureRandom = new SecureRandom();
    }

    public String createRefreshToken(String userId, String userAgent) {
        String raw = generate();

        RefreshToken token = RefreshToken
                .builder()
                .userId(userId)
                .hash(hasher.hash(raw))
                .expiresAt(generateExpirationDate())
                .familyId(UUID.randomUUID().toString())
                .userAgent(userAgent)
                .build();

        repository.save(token);

        return raw;
    }

    public void refresh() {

    }

    private String generate() {
        byte[] randomBytes = new byte[tokenSize];
        secureRandom.nextBytes(randomBytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
    }

    private Instant generateExpirationDate() {
        return Instant.now().plusSeconds(60 * 60 * 24 * 7);
    }

    private String recoverUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }
}
