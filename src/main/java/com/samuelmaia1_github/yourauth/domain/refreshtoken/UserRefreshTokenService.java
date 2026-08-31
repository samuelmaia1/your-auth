package com.samuelmaia1_github.yourauth.domain.refreshtoken;

import com.samuelmaia1_github.yourauth.domain.auth.exceptions.InvalidTokenException;
import com.samuelmaia1_github.yourauth.domain.project.authconfig.AuthConfig;
import com.samuelmaia1_github.yourauth.domain.project.authconfig.AuthConfigRepository;
import com.samuelmaia1_github.yourauth.domain.project.authconfig.exceptions.AuthConfigNotFoundException;
import com.samuelmaia1_github.yourauth.domain.refreshtoken.exceptions.ExpiredRefreshTokenException;
import com.samuelmaia1_github.yourauth.domain.refreshtoken.exceptions.RefreshTokenReuseException;
import com.samuelmaia1_github.yourauth.domain.usersession.UserSession;
import com.samuelmaia1_github.yourauth.domain.usersession.UserSessionRepository;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.user.TokenDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.user.UserRefreshResponseDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class UserRefreshTokenService {
    private final RefreshTokenHasher hasher;
    private final RefreshTokenGenerator generator;
    private final UserRefreshTokenRepository repository;
    private final AuthConfigRepository authConfigRepository;
    private final UserSessionRepository sessionRepository;

    public TokenDTO createUserRefreshToken(String projectId, String userId, String sessionId, String userAgent) {
        AuthConfig auth = authConfigRepository.findByProjectId(projectId)
                .orElseThrow(AuthConfigNotFoundException::new);

        String raw = generator.generate();

        UserRefreshToken refreshToken = UserRefreshToken
                .builder()
                .userId(userId)
                .projectId(projectId)
                .expiresAt(getExpiration(auth.getRefreshTokenExpirationDays()))
                .sessionId(sessionId)
                .userAgent(userAgent)
                .hash(hasher.hash(raw))
                .build();

        repository.save(refreshToken);

        return new TokenDTO(raw, Duration.ofDays(auth.getRefreshTokenExpirationDays()));
    }

    @Transactional
    public UserRefreshResponseDTO refresh(String currentRawToken) {
        UserRefreshToken currentToken = getToken(hash(currentRawToken));

        validateToken(currentToken);
        validateSession(currentToken);

        currentToken.revoke();

        AuthConfig auth = authConfigRepository.findByProjectId(currentToken.getProjectId())
                .orElseThrow(AuthConfigNotFoundException::new);

        String newRaw = generator.generate();
        Duration duration = Duration.ofDays(auth.getRefreshTokenExpirationDays());

        UserRefreshToken newToken = UserRefreshToken
                .builder()
                .projectId(currentToken.getProjectId())
                .userId(currentToken.getUserId())
                .sessionId(currentToken.getSessionId())
                .userAgent(currentToken.getUserAgent())
                .expiresAt(Instant.now().plus(duration))
                .hash(hasher.hash(newRaw))
                .build();

        repository.save(currentToken);
        repository.save(newToken);

        return new UserRefreshResponseDTO(
                currentToken.getProjectId(),
                currentToken.getUserId(),
                currentToken.getSessionId(),
                new TokenDTO(newRaw, duration)
        );
    }

    public UserRefreshToken getToken(String hash) {
        return repository.findByHash(hash)
                .orElseThrow(() -> new InvalidTokenException("Refresh token não existente"));
    }

    @Transactional
    public void logout(String currentRawToken) {
        UserRefreshToken currentToken = getToken(hash(currentRawToken));
        System.out.println(currentToken.getSessionId());
    }

    @Transactional
    public void revokeSession(String sessionId) {
        repository.revokeSession(sessionId);
        sessionRepository.revokeById(sessionId);
    }

    private Instant getExpiration(Integer days) {
        Duration duration = Duration.ofDays(days);
        return Instant.now().plus(duration);
    }

    private void validateToken(UserRefreshToken token) {
        if (token.isRevoked()) {
            revokeSession(token.getSessionId());

            throw new RefreshTokenReuseException("Refresh token reutilizado. A sessão foi encerrada.");
        }

        if (token.isExpired()) {
            throw new ExpiredRefreshTokenException("Refresh token expirado ou revogado");
        }
    }

    private void validateSession(UserRefreshToken token) {
        UserSession session = sessionRepository.findById(token.getSessionId())
                .orElse(null);

        if (session == null) {
            revokeSession(token.getSessionId());

            throw new InvalidTokenException("Sessão inválida ou expirada.");
        }

        boolean isSameUserSession = token.getProjectId().equals(session.getProjectId())
                && token.getUserId().equals(session.getUserId());

        if (!isSameUserSession || !session.isValid()) {
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
}
