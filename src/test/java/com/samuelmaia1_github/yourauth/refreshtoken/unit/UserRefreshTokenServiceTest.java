package com.samuelmaia1_github.yourauth.refreshtoken.unit;

import com.samuelmaia1_github.yourauth.domain.auth.exceptions.InvalidTokenException;
import com.samuelmaia1_github.yourauth.domain.project.authconfig.AuthConfig;
import com.samuelmaia1_github.yourauth.domain.project.authconfig.AuthConfigRepository;
import com.samuelmaia1_github.yourauth.domain.project.authconfig.SessionMode;
import com.samuelmaia1_github.yourauth.domain.projectapikey.exceptions.ProjectApiKeyAccessDeniedException;
import com.samuelmaia1_github.yourauth.domain.refreshtoken.RefreshTokenGenerator;
import com.samuelmaia1_github.yourauth.domain.refreshtoken.RefreshTokenHasher;
import com.samuelmaia1_github.yourauth.domain.refreshtoken.UserRefreshToken;
import com.samuelmaia1_github.yourauth.domain.refreshtoken.UserRefreshTokenRepository;
import com.samuelmaia1_github.yourauth.domain.refreshtoken.UserRefreshTokenService;
import com.samuelmaia1_github.yourauth.domain.refreshtoken.exceptions.RefreshTokenReuseException;
import com.samuelmaia1_github.yourauth.domain.usersession.UserSession;
import com.samuelmaia1_github.yourauth.domain.usersession.UserSessionRepository;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.user.UserRefreshResponseDTO;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserRefreshTokenServiceTest {
    private static final String PROJECT_ID = "project-id";
    private static final String USER_ID = "user-id";
    private static final String SESSION_ID = "session-id";

    @Test
    void shouldRotateRefreshTokenKeepingCurrentSession() {
        RefreshTokenHasher hasher = new RefreshTokenHasher("test-secret");
        InMemoryUserRefreshTokenRepository refreshTokenRepository = new InMemoryUserRefreshTokenRepository();
        RecordingUserSessionRepository sessionRepository = new RecordingUserSessionRepository(activeSession());
        UserRefreshToken currentToken = currentToken(hasher, null);
        refreshTokenRepository.save(currentToken);
        refreshTokenRepository.savedTokens.clear();
        UserRefreshTokenService service = service(
                hasher,
                new FixedRefreshTokenGenerator("new-refresh-token"),
                refreshTokenRepository,
                sessionRepository
        );

        UserRefreshResponseDTO response = service.refresh("current-refresh-token", PROJECT_ID);

        assertThat(response.projectId()).isEqualTo(PROJECT_ID);
        assertThat(response.userId()).isEqualTo(USER_ID);
        assertThat(response.sessionId()).isEqualTo(SESSION_ID);
        assertThat(response.refreshToken().raw()).isEqualTo("new-refresh-token");
        assertThat(response.refreshToken().duration()).isEqualTo(Duration.ofDays(7));
        assertThat(refreshTokenRepository.savedTokens).hasSize(2);
        assertThat(currentToken.isRevoked()).isTrue();

        UserRefreshToken newToken = refreshTokenRepository
                .findByHash(hasher.hash("new-refresh-token"))
                .orElseThrow();
        assertThat(newToken.getSessionId()).isEqualTo(SESSION_ID);
        assertThat(newToken.getProjectId()).isEqualTo(PROJECT_ID);
        assertThat(newToken.getUserId()).isEqualTo(USER_ID);
        assertThat(newToken.isValid()).isTrue();
        assertThat(sessionRepository.revokedSessionId).isNull();
    }

    @Test
    void shouldRevokeSessionWhenRefreshTokenIsReused() {
        RefreshTokenHasher hasher = new RefreshTokenHasher("test-secret");
        InMemoryUserRefreshTokenRepository refreshTokenRepository = new InMemoryUserRefreshTokenRepository();
        RecordingUserSessionRepository sessionRepository = new RecordingUserSessionRepository(activeSession());
        refreshTokenRepository.save(currentToken(hasher, Instant.now()));
        UserRefreshTokenService service = service(
                hasher,
                new FixedRefreshTokenGenerator("new-refresh-token"),
                refreshTokenRepository,
                sessionRepository
        );

        assertThatThrownBy(() -> service.refresh("current-refresh-token", PROJECT_ID))
                .isInstanceOf(RefreshTokenReuseException.class)
                .hasMessage("Refresh token reutilizado. A sessão foi encerrada.");

        assertThat(refreshTokenRepository.revokedSessionId).isEqualTo(SESSION_ID);
        assertThat(sessionRepository.revokedSessionId).isEqualTo(SESSION_ID);
    }

    @Test
    void shouldRevokeRefreshTokensWhenSessionDoesNotExist() {
        RefreshTokenHasher hasher = new RefreshTokenHasher("test-secret");
        InMemoryUserRefreshTokenRepository refreshTokenRepository = new InMemoryUserRefreshTokenRepository();
        RecordingUserSessionRepository sessionRepository = new RecordingUserSessionRepository(null);
        refreshTokenRepository.save(currentToken(hasher, null));
        UserRefreshTokenService service = service(
                hasher,
                new FixedRefreshTokenGenerator("new-refresh-token"),
                refreshTokenRepository,
                sessionRepository
        );

        assertThatThrownBy(() -> service.refresh("current-refresh-token", PROJECT_ID))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("Sessão inválida ou expirada.");

        assertThat(refreshTokenRepository.revokedSessionId).isEqualTo(SESSION_ID);
        assertThat(sessionRepository.revokedSessionId).isEqualTo(SESSION_ID);
    }

    @Test
    void shouldRevokeSessionAndCurrentRefreshTokensOnLogout() {
        RefreshTokenHasher hasher = new RefreshTokenHasher("test-secret");
        InMemoryUserRefreshTokenRepository refreshTokenRepository = new InMemoryUserRefreshTokenRepository();
        RecordingUserSessionRepository sessionRepository = new RecordingUserSessionRepository(activeSession());
        refreshTokenRepository.save(currentToken(hasher, null));
        UserRefreshTokenService service = service(
                hasher,
                new FixedRefreshTokenGenerator("new-refresh-token"),
                refreshTokenRepository,
                sessionRepository
        );

        service.logout("current-refresh-token", PROJECT_ID);

        assertThat(refreshTokenRepository.revokedSessionId).isEqualTo(SESSION_ID);
        assertThat(sessionRepository.revokedSessionId).isEqualTo(SESSION_ID);
        assertThat(refreshTokenRepository.findByHash(hasher.hash("current-refresh-token")).orElseThrow().isRevoked())
                .isTrue();
    }

    @Test
    void shouldDenyRefreshWhenApiKeyBelongsToAnotherProject() {
        RefreshTokenHasher hasher = new RefreshTokenHasher("test-secret");
        InMemoryUserRefreshTokenRepository refreshTokenRepository = new InMemoryUserRefreshTokenRepository();
        RecordingUserSessionRepository sessionRepository = new RecordingUserSessionRepository(activeSession());
        UserRefreshToken currentToken = currentToken(hasher, null);
        refreshTokenRepository.save(currentToken);
        UserRefreshTokenService service = service(
                hasher,
                new FixedRefreshTokenGenerator("new-refresh-token"),
                refreshTokenRepository,
                sessionRepository
        );

        assertThatThrownBy(() -> service.refresh("current-refresh-token", "another-project-id"))
                .isInstanceOf(ProjectApiKeyAccessDeniedException.class)
                .hasMessage("A API key não tem permissão para gerenciar sessões deste projeto.");

        assertThat(refreshTokenRepository.revokedSessionId).isNull();
        assertThat(sessionRepository.revokedSessionId).isNull();
        assertThat(currentToken.isRevoked()).isFalse();
    }

    @Test
    void shouldDenyLogoutWhenApiKeyBelongsToAnotherProject() {
        RefreshTokenHasher hasher = new RefreshTokenHasher("test-secret");
        InMemoryUserRefreshTokenRepository refreshTokenRepository = new InMemoryUserRefreshTokenRepository();
        RecordingUserSessionRepository sessionRepository = new RecordingUserSessionRepository(activeSession());
        UserRefreshToken currentToken = currentToken(hasher, null);
        refreshTokenRepository.save(currentToken);
        UserRefreshTokenService service = service(
                hasher,
                new FixedRefreshTokenGenerator("new-refresh-token"),
                refreshTokenRepository,
                sessionRepository
        );

        assertThatThrownBy(() -> service.logout("current-refresh-token", "another-project-id"))
                .isInstanceOf(ProjectApiKeyAccessDeniedException.class)
                .hasMessage("A API key não tem permissão para gerenciar sessões deste projeto.");

        assertThat(refreshTokenRepository.revokedSessionId).isNull();
        assertThat(sessionRepository.revokedSessionId).isNull();
        assertThat(currentToken.isRevoked()).isFalse();
    }

    private UserRefreshTokenService service(
            RefreshTokenHasher hasher,
            RefreshTokenGenerator generator,
            UserRefreshTokenRepository refreshTokenRepository,
            UserSessionRepository sessionRepository
    ) {
        return new UserRefreshTokenService(
                hasher,
                generator,
                refreshTokenRepository,
                new StubAuthConfigRepository(),
                sessionRepository
        );
    }

    private UserRefreshToken currentToken(RefreshTokenHasher hasher, Instant revokedAt) {
        return UserRefreshToken.builder()
                .id("refresh-token-id")
                .projectId(PROJECT_ID)
                .userId(USER_ID)
                .hash(hasher.hash("current-refresh-token"))
                .sessionId(SESSION_ID)
                .expiresAt(Instant.now().plus(Duration.ofDays(1)))
                .revokedAt(revokedAt)
                .userAgent("user-agent")
                .build();
    }

    private static UserSession activeSession() {
        return UserSession.builder()
                .id(SESSION_ID)
                .projectId(PROJECT_ID)
                .userId(USER_ID)
                .lastUsedAt(Instant.now())
                .build();
    }

    private static class FixedRefreshTokenGenerator extends RefreshTokenGenerator {
        private final String token;

        private FixedRefreshTokenGenerator(String token) {
            this.token = token;
        }

        @Override
        public String generate() {
            return token;
        }
    }

    private static class StubAuthConfigRepository implements AuthConfigRepository {
        @Override
        public AuthConfig save(AuthConfig config) {
            return config;
        }

        @Override
        public Optional<AuthConfig> findByProjectId(String projectId) {
            return Optional.of(AuthConfig.builder()
                    .projectId(projectId)
                    .accessTokenExpirationMinutes(15)
                    .refreshTokenExpirationDays(7)
                    .sessionMode(SessionMode.MULTIPLE_DEVICES)
                    .failedLoginAttemptsLimit(5)
                    .lockDurationMinutes(15)
                    .build());
        }
    }

    private static class InMemoryUserRefreshTokenRepository implements UserRefreshTokenRepository {
        private final List<UserRefreshToken> tokens = new ArrayList<>();
        private final List<UserRefreshToken> savedTokens = new ArrayList<>();
        private String revokedSessionId;

        @Override
        public UserRefreshToken save(UserRefreshToken refreshToken) {
            tokens.removeIf(token -> token.getHash().equals(refreshToken.getHash()));
            tokens.add(refreshToken);
            savedTokens.add(refreshToken);
            return refreshToken;
        }

        @Override
        public Optional<UserRefreshToken> findById(String id) {
            return tokens.stream()
                    .filter(token -> id.equals(token.getId()))
                    .findFirst();
        }

        @Override
        public Optional<UserRefreshToken> findByHash(String hash) {
            return tokens.stream()
                    .filter(token -> hash.equals(token.getHash()))
                    .findFirst();
        }

        @Override
        public List<UserRefreshToken> findAllByProjectIdAndUserId(String projectId, String userId) {
            return tokens.stream()
                    .filter(token -> projectId.equals(token.getProjectId()))
                    .filter(token -> userId.equals(token.getUserId()))
                    .toList();
        }

        @Override
        public List<UserRefreshToken> findAllBySessionId(String sessionId) {
            return tokens.stream()
                    .filter(token -> sessionId.equals(token.getSessionId()))
                    .toList();
        }

        @Override
        public void revokeSession(String sessionId) {
            revokedSessionId = sessionId;
            findAllBySessionId(sessionId).stream()
                    .filter(token -> !token.isRevoked())
                    .forEach(UserRefreshToken::revoke);
        }

        @Override
        public void deleteById(String id) {
            tokens.removeIf(token -> id.equals(token.getId()));
        }
    }

    private static class RecordingUserSessionRepository implements UserSessionRepository {
        private final UserSession session;
        private String revokedSessionId;

        private RecordingUserSessionRepository(UserSession session) {
            this.session = session;
        }

        @Override
        public UserSession save(UserSession userSession) {
            return userSession;
        }

        @Override
        public Optional<UserSession> findById(String id) {
            if (session != null && id.equals(session.getId())) {
                return Optional.of(session);
            }

            return Optional.empty();
        }

        @Override
        public List<UserSession> findAllByProjectIdAndUserId(String projectId, String userId) {
            return List.of();
        }

        @Override
        public List<UserSession> findAllByProjectIdAndUserIdAndRevokedAtIsNull(String projectId, String userId) {
            return List.of();
        }

        @Override
        public void revokeById(String id) {
            revokedSessionId = id;
            if (session != null && id.equals(session.getId())) {
                session.revoke();
            }
        }

        @Override
        public long countByProjectIdAndUserIdAndRevokedAtIsNull(String projectId, String userId) {
            return 0;
        }
    }
}
