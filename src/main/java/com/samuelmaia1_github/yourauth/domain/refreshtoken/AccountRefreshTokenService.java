package com.samuelmaia1_github.yourauth.domain.refreshtoken;

import com.samuelmaia1_github.yourauth.domain.auth.exceptions.InvalidTokenException;
import com.samuelmaia1_github.yourauth.domain.accountsession.AccountSession;
import com.samuelmaia1_github.yourauth.domain.accountsession.AccountSessionRepository;
import com.samuelmaia1_github.yourauth.domain.refreshtoken.exceptions.ExpiredRefreshTokenException;
import com.samuelmaia1_github.yourauth.domain.refreshtoken.exceptions.RefreshTokenReuseException;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.AccountRefreshResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.user.TokenDTO;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

@Service
public class AccountRefreshTokenService {

    private final RefreshTokenHasher hasher;
    private final RefreshTokenGenerator generator;
    private final AccountRefreshTokenRepository repository;
    private final AccountSessionRepository sessionRepository;
    private final Duration refreshTokenDuration;

    public AccountRefreshTokenService(
            RefreshTokenHasher hasher,
            RefreshTokenGenerator generator,
            AccountRefreshTokenRepository repository,
            AccountSessionRepository sessionRepository,
            @Value("${api.security.account-refresh-token.duration:${api.security.refresh-token.duration}}")
            Duration refreshTokenDuration
    ) {
        this.hasher = hasher;
        this.generator = generator;
        this.repository = repository;
        this.sessionRepository = sessionRepository;
        this.refreshTokenDuration = refreshTokenDuration;
    }

    public TokenDTO createAccountRefreshToken(String accountId, String sessionId, String userAgent) {
        String raw = generator.generate();
        String hash = hasher.hash(raw);

        AccountRefreshToken token = AccountRefreshToken
                .builder()
                .accountId(accountId)
                .hash(hash)
                .expiresAt(generateExpirationDate())
                .sessionId(sessionId)
                .userAgent(userAgent)
                .build();

        repository.save(token);

        return new TokenDTO(raw, refreshTokenDuration);
    }

    @Transactional
    public AccountRefreshResponseDTO refresh(String currentRawToken) {
        String currentHash = hash(currentRawToken);
        AccountRefreshToken currentToken = getToken(currentHash);

        validateToken(currentToken);
        validateSession(currentToken);

        currentToken.revoke();

        String newRaw = generator.generate();
        String newHash = hasher.hash(newRaw);

        AccountRefreshToken newToken = AccountRefreshToken
                .builder()
                .accountId(currentToken.getAccountId())
                .userAgent(currentToken.getUserAgent())
                .expiresAt(generateExpirationDate())
                .hash(newHash)
                .sessionId(currentToken.getSessionId())
                .build();

        repository.save(currentToken);
        repository.save(newToken);

        return new AccountRefreshResponseDTO(
                currentToken.getAccountId(),
                currentToken.getSessionId(),
                new TokenDTO(newRaw, refreshTokenDuration)
        );
    }

    public AccountRefreshToken getToken(String hash) {
        Optional<AccountRefreshToken> optionalRefreshToken = repository.findByHash(hash);

        if (optionalRefreshToken.isEmpty()) {
            throw new InvalidTokenException("Refresh token não existente");
        }

        return optionalRefreshToken.get();
    }

    @Transactional
    public void logout(String currentRawToken) {
        AccountRefreshToken currentToken = getToken(hash(currentRawToken));

        revokeSession(currentToken.getSessionId());
    }

    @Transactional
    public void revokeSession(String sessionId) {
        repository.revokeSession(sessionId);
        sessionRepository.revokeById(sessionId);
    }

    private void validateToken(AccountRefreshToken token) {
        if (token.isRevoked()) {
            revokeSession(token.getSessionId());

            throw new RefreshTokenReuseException("Refresh token reutilizado. A sessão foi encerrada.");
        }

        if (token.isExpired()) {
            throw new ExpiredRefreshTokenException("Refresh token expirado ou revogado");
        }
    }

    private void validateSession(AccountRefreshToken token) {
        AccountSession session = sessionRepository.findById(token.getSessionId())
                .orElse(null);

        if (session == null) {
            revokeSession(token.getSessionId());

            throw new InvalidTokenException("Sessão inválida ou expirada.");
        }

        boolean isSameAccountSession = Objects.equals(token.getAccountId(), session.getAccountId());

        if (!isSameAccountSession || !session.isValid()) {
            revokeSession(token.getSessionId());

            throw new InvalidTokenException("Sessão inválida ou expirada.");
        }
    }

    private String hash(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new InvalidTokenException("Refresh token não informado");
        }

        return hasher.hash(rawToken);
    }

    private Instant generateExpirationDate() {
        return Instant.now().plus(refreshTokenDuration);
    }

}
