package com.samuelmaia1_github.yourauth.auth.unit;

import com.samuelmaia1_github.yourauth.domain.auth.TokenService;
import com.samuelmaia1_github.yourauth.domain.auth.UserAuthService;
import com.samuelmaia1_github.yourauth.domain.project.authconfig.AuthConfig;
import com.samuelmaia1_github.yourauth.domain.project.authconfig.AuthConfigRepository;
import com.samuelmaia1_github.yourauth.domain.project.authconfig.SessionMode;
import com.samuelmaia1_github.yourauth.domain.refreshtoken.UserRefreshTokenService;
import com.samuelmaia1_github.yourauth.domain.shared.PageResult;
import com.samuelmaia1_github.yourauth.domain.shared.Pagination;
import com.samuelmaia1_github.yourauth.domain.user.User;
import com.samuelmaia1_github.yourauth.domain.user.UserRepository;
import com.samuelmaia1_github.yourauth.domain.user.UserStatus;
import com.samuelmaia1_github.yourauth.domain.usersession.UserSession;
import com.samuelmaia1_github.yourauth.domain.usersession.UserSessionPolicy;
import com.samuelmaia1_github.yourauth.domain.usersession.UserSessionRepository;
import com.samuelmaia1_github.yourauth.infra.interfaces.IPasswordEncoder;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.user.TokenDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.user.UserLoginDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.user.UserLoginResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.user.UserLoginSessionDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.user.UserRefreshResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.user.UserTokensResponseDTO;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class UserAuthServiceTest {
    private static final String PROJECT_ID = "project-id";
    private static final String USER_ID = "user-id";
    private static final String SESSION_ID = "session-id";

    @Test
    void shouldCreateRefreshTokenUsingSavedSessionIdOnLogin() {
        RecordingUserSessionRepository sessionRepository = new RecordingUserSessionRepository(null);
        StubUserRefreshTokenService refreshTokenService = new StubUserRefreshTokenService();
        UserAuthService service = service(
                new StubUserRepository(user()),
                sessionRepository,
                refreshTokenService
        );

        UserLoginSessionDTO loginSession = service.login(
                new UserLoginDTO("user@email.com", "raw-password"),
                PROJECT_ID,
                "127.0.0.1",
                "user-agent",
                "device"
        );
        UserLoginResponseDTO response = loginSession.response();

        assertThat(response.token().raw()).isEqualTo("access-token");
        assertThat(loginSession.refreshToken().raw()).isEqualTo("created-refresh-token");
        assertThat(refreshTokenService.createdProjectId).isEqualTo(PROJECT_ID);
        assertThat(refreshTokenService.createdUserId).isEqualTo(USER_ID);
        assertThat(refreshTokenService.createdSessionId).isEqualTo(SESSION_ID);
        assertThat(refreshTokenService.createdUserAgent).isEqualTo("user-agent");
        assertThat(sessionRepository.session.getId()).isEqualTo(SESSION_ID);
    }

    @Test
    void shouldRefreshUserSessionAndUpdateLastUsedAt() {
        Instant previousLastUsedAt = Instant.now().minus(Duration.ofHours(1));
        UserSession session = UserSession.builder()
                .id(SESSION_ID)
                .projectId(PROJECT_ID)
                .userId(USER_ID)
                .lastUsedAt(previousLastUsedAt)
                .build();
        RecordingUserSessionRepository sessionRepository = new RecordingUserSessionRepository(session);
        StubUserRefreshTokenService refreshTokenService = new StubUserRefreshTokenService();
        UserAuthService service = service(
                new StubUserRepository(user()),
                sessionRepository,
                refreshTokenService
        );

        UserTokensResponseDTO tokens = service.refreshUserSession("current-refresh-token");

        assertThat(refreshTokenService.refreshedRawToken).isEqualTo("current-refresh-token");
        assertThat(tokens.accessToken().raw()).isEqualTo("access-token");
        assertThat(tokens.accessToken().duration()).isEqualTo(Duration.ofMinutes(20));
        assertThat(tokens.refreshToken().raw()).isEqualTo("new-refresh-token");
        assertThat(sessionRepository.savedSession.getLastUsedAt()).isAfter(previousLastUsedAt);
        assertThat(sessionRepository.session.isRevoked()).isFalse();
    }

    private UserAuthService service(
            UserRepository userRepository,
            RecordingUserSessionRepository sessionRepository,
            StubUserRefreshTokenService refreshTokenService
    ) {
        return new UserAuthService(
                userRepository,
                new StubAuthConfigRepository(),
                sessionRepository,
                new UserSessionPolicy(sessionRepository),
                new MatchingPasswordEncoder(),
                new StubTokenService(),
                refreshTokenService
        );
    }

    private static User user() {
        return User.builder()
                .id(USER_ID)
                .projectId(PROJECT_ID)
                .email("user@email.com")
                .password("encoded-password")
                .status(UserStatus.ACTIVE)
                .failedLoginAttempts(0)
                .build();
    }

    private static class StubUserRefreshTokenService extends UserRefreshTokenService {
        private String createdProjectId;
        private String createdUserId;
        private String createdSessionId;
        private String createdUserAgent;
        private String refreshedRawToken;

        private StubUserRefreshTokenService() {
            super(null, null, null, null, null);
        }

        @Override
        public TokenDTO createUserRefreshToken(String projectId, String userId, String sessionId, String userAgent) {
            createdProjectId = projectId;
            createdUserId = userId;
            createdSessionId = sessionId;
            createdUserAgent = userAgent;

            return new TokenDTO("created-refresh-token", Duration.ofDays(7));
        }

        @Override
        public UserRefreshResponseDTO refresh(String currentRawToken) {
            refreshedRawToken = currentRawToken;

            return new UserRefreshResponseDTO(
                    PROJECT_ID,
                    USER_ID,
                    SESSION_ID,
                    new TokenDTO("new-refresh-token", Duration.ofDays(7))
            );
        }
    }

    private static class StubTokenService extends TokenService {
        private StubTokenService() {
            super("secret", "issuer", Duration.ofMinutes(15));
        }

        @Override
        public String generateToken(User user, String projectId, AuthConfig config) {
            return "access-token";
        }
    }

    private static class MatchingPasswordEncoder implements IPasswordEncoder {
        @Override
        public String encode(String raw) {
            return "encoded-password";
        }

        @Override
        public Boolean matches(String raw, String hash) {
            return "raw-password".equals(raw) && "encoded-password".equals(hash);
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
                    .accessTokenExpirationMinutes(20)
                    .refreshTokenExpirationDays(7)
                    .sessionMode(SessionMode.MULTIPLE_DEVICES)
                    .failedLoginAttemptsLimit(5)
                    .lockDurationMinutes(15)
                    .build());
        }
    }

    private static class StubUserRepository implements UserRepository {
        private final User user;

        private StubUserRepository(User user) {
            this.user = user;
        }

        @Override
        public User save(User user) {
            return user;
        }

        @Override
        public Optional<User> findById(String id) {
            return Optional.empty();
        }

        @Override
        public Optional<User> findByProjectIdAndId(String projectId, String id) {
            if (PROJECT_ID.equals(projectId) && USER_ID.equals(id)) {
                return Optional.of(user);
            }

            return Optional.empty();
        }

        @Override
        public Optional<User> findByProjectIdAndEmailIgnoreCase(String projectId, String email) {
            if (PROJECT_ID.equals(projectId) && "user@email.com".equalsIgnoreCase(email)) {
                return Optional.of(user);
            }

            return Optional.empty();
        }

        @Override
        public boolean existsByProjectIdAndEmailIgnoreCase(String projectId, String email) {
            return false;
        }

        @Override
        public boolean existsByProjectIdAndEmailIgnoreCaseAndIdNot(String projectId, String email, String id) {
            return false;
        }

        @Override
        public PageResult<User> findAllByProjectId(String projectId, Pagination pagination) {
            return new PageResult<>(List.of(), pagination.page(), pagination.size(), 0, 0);
        }

        @Override
        public void deleteById(String id) {
        }
    }

    private static class RecordingUserSessionRepository implements UserSessionRepository {
        private UserSession session;
        private UserSession savedSession;

        private RecordingUserSessionRepository(UserSession session) {
            this.session = session;
        }

        @Override
        public UserSession save(UserSession userSession) {
            savedSession = userSession;

            if (userSession.getId() != null) {
                session = userSession;
                return userSession;
            }

            session = UserSession.builder()
                    .id(SESSION_ID)
                    .projectId(userSession.getProjectId())
                    .userId(userSession.getUserId())
                    .deviceName(userSession.getDeviceName())
                    .ipAddress(userSession.getIpAddress())
                    .userAgent(userSession.getUserAgent())
                    .lastUsedAt(userSession.getLastUsedAt())
                    .build();

            return session;
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
