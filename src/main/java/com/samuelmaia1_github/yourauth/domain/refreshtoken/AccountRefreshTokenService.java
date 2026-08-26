package com.samuelmaia1_github.yourauth.domain.refreshtoken;

import com.samuelmaia1_github.yourauth.domain.auth.exceptions.InvalidTokenException;
import com.samuelmaia1_github.yourauth.domain.refreshtoken.exceptions.ExpiredRefreshTokenException;
import com.samuelmaia1_github.yourauth.domain.refreshtoken.exceptions.RefreshTokenReuseException;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.AccountRefreshResponseDTO;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class AccountRefreshTokenService {

    private final RefreshTokenHasher hasher;
    private final RefreshTokenGenerator generator;
    private final AccountRefreshTokenRepository repository;
    private final Duration refreshTokenDuration;

    public AccountRefreshTokenService(
            RefreshTokenHasher hasher,
            RefreshTokenGenerator generator,
            AccountRefreshTokenRepository repository,
            @Value("${api.security.account-refresh-token.duration:${api.security.refresh-token.duration}}")
            Duration refreshTokenDuration
    ) {
        this.hasher = hasher;
        this.generator = generator;
        this.repository = repository;
        this.refreshTokenDuration = refreshTokenDuration;
    }

    public String createAccountRefreshToken(String accountId, String userAgent) {
        String raw = generator.generate();

        AccountRefreshToken token = AccountRefreshToken
                .builder()
                .accountId(accountId)
                .hash(hasher.hash(raw))
                .expiresAt(generateExpirationDate())
                .sessionId(UUID.randomUUID().toString())
                .userAgent(userAgent)
                .build();

        repository.save(token);

        return raw;
    }

    @Transactional
    public AccountRefreshResponseDTO refresh(String currentRawToken) {
        AccountRefreshToken currentToken = getToken(hasher.hash(currentRawToken));

        validateToken(currentToken);

        currentToken.revoke();

        String newRaw = generator.generate();

        AccountRefreshToken newToken = AccountRefreshToken
                .builder()
                .accountId(currentToken.getAccountId())
                .userAgent(currentToken.getUserAgent())
                .expiresAt(generateExpirationDate())
                .hash(hasher.hash(newRaw))
                .sessionId(currentToken.getSessionId())
                .build();

        repository.save(currentToken);
        repository.save(newToken);

        return new AccountRefreshResponseDTO(currentToken.getAccountId(), newRaw);
    }

    public AccountRefreshToken getToken(String hash) {
        Optional<AccountRefreshToken> optionalRefreshToken = repository.findByHash(hash);

        if (optionalRefreshToken.isEmpty())
            throw new InvalidTokenException("Refresh token não existente");

        return optionalRefreshToken.get();
    }

    public void revokeSession(String sessionId) {
        repository.revokeSession(sessionId);
    }

    private void validateToken(AccountRefreshToken token) {
        if (token.isRevoked()) {
            revokeSession(token.getSessionId());

            throw new RefreshTokenReuseException("Refresh token reutilizado. A sessão foi encerrada.");
        }

        if (token.isExpired())
            throw new ExpiredRefreshTokenException("Refresh token expirado ou revogado");
    }

    private Instant generateExpirationDate() {
        return Instant.now().plus(refreshTokenDuration);
    }

}
