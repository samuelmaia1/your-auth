package com.samuelmaia1_github.yourauth.domain.refreshtoken;

import com.samuelmaia1_github.yourauth.domain.auth.exceptions.InvalidTokenException;
import com.samuelmaia1_github.yourauth.domain.refreshtoken.exceptions.ExpiredRefreshTokenException;
import com.samuelmaia1_github.yourauth.domain.refreshtoken.exceptions.RefreshTokenReuseException;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.RefreshResponseDTO;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
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

    @Transactional
    public RefreshResponseDTO refresh(String currentRawToken) {
        RefreshToken currentToken = getToken(hasher.hash(currentRawToken));

        validateToken(currentToken);

        currentToken.revoke();

        String newRaw = generate();

        RefreshToken newToken = RefreshToken
                .builder()
                .userId(currentToken.getUserId())
                .userAgent(currentToken.getUserAgent())
                .expiresAt(generateExpirationDate())
                .hash(hasher.hash(newRaw))
                .familyId(currentToken.getFamilyId())
                .build();

        repository.save(currentToken);
        repository.save(newToken);

        return new RefreshResponseDTO(currentToken.getUserId(), newRaw);
    }

    private String generate() {
        byte[] randomBytes = new byte[tokenSize];
        secureRandom.nextBytes(randomBytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
    }

    public RefreshToken getToken(String hash) {
        Optional<RefreshToken> optionalRefreshToken = repository.findByHash(hash);

        if (optionalRefreshToken.isEmpty())
            throw new InvalidTokenException("Refresh token não existente");

        return optionalRefreshToken.get();
    }

    public void revokeFamily(String familyId) {
        repository.revokeFamily(familyId);
    }

    private void validateToken(RefreshToken token) {
        if (token.isRevoked()) {
            revokeFamily(token.getFamilyId());

            throw new RefreshTokenReuseException("Refresh token reutilizado. A sessão foi encerrada.");
        }

        if (token.isExpired())
            throw new ExpiredRefreshTokenException("Refresh token expirado ou revogado");
    }

    private Instant generateExpirationDate() {
        return Instant.now().plusSeconds(60 * 60 * 24 * 7);
    }

}
