package com.samuelmaia1_github.yourauth.auth.unit;

import com.samuelmaia1_github.yourauth.domain.account.Account;
import com.samuelmaia1_github.yourauth.domain.accountsession.AccountSession;
import com.samuelmaia1_github.yourauth.domain.accountsession.AccountSessionRepository;
import com.samuelmaia1_github.yourauth.domain.auth.TokenService;
import com.samuelmaia1_github.yourauth.domain.auth.exceptions.GenerateTokenFailException;
import com.samuelmaia1_github.yourauth.domain.project.authconfig.AuthConfig;
import com.samuelmaia1_github.yourauth.domain.user.User;
import com.samuelmaia1_github.yourauth.domain.user.UserStatus;
import com.samuelmaia1_github.yourauth.domain.usersession.UserSession;
import com.samuelmaia1_github.yourauth.domain.usersession.UserSessionRepository;
import com.samuelmaia1_github.yourauth.domain.valueobjects.CPF;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TokenServiceTest {
    private static final String ACCOUNT_ID = "account-id";
    private static final String USER_ID = "user-id";
    private static final String PROJECT_ID = "project-id";
    private static final String SESSION_ID = "session-id";

    @Test
    void shouldInvalidateAccountAccessTokenWhenSessionIsRevoked() {
        AccountSession session = AccountSession.builder()
                .id(SESSION_ID)
                .accountId(ACCOUNT_ID)
                .build();
        TokenService service = new TokenService(
                "secret",
                "issuer",
                Duration.ofMinutes(15),
                new InMemoryAccountSessionRepository(session),
                new InMemoryUserSessionRepository(null)
        );

        String token = service.generateToken(account(), SESSION_ID);

        assertThat(service.getSessionId(token)).isEqualTo(SESSION_ID);
        assertThat(service.isValidAccountAccessToken(token)).isTrue();

        session.revoke();

        assertThat(service.isValidAccountAccessToken(token)).isFalse();
    }

    @Test
    void shouldInvalidateUserAccessTokenWhenSessionIsRevoked() {
        UserSession session = UserSession.builder()
                .id(SESSION_ID)
                .projectId(PROJECT_ID)
                .userId(USER_ID)
                .build();
        TokenService service = new TokenService(
                "secret",
                "issuer",
                Duration.ofMinutes(15),
                new InMemoryAccountSessionRepository(null),
                new InMemoryUserSessionRepository(session)
        );

        String token = service.generateToken(user(), PROJECT_ID, authConfig(), SESSION_ID);

        assertThat(service.getSessionId(token)).isEqualTo(SESSION_ID);
        assertThat(service.isValidUserAccessToken(token)).isTrue();

        session.revoke();

        assertThat(service.isValidUserAccessToken(token)).isFalse();
    }

    @Test
    void shouldNotGenerateAccountAccessTokenWithoutSessionId() {
        TokenService service = tokenServiceWithoutSessions();

        assertThatThrownBy(() -> service.generateToken(account(), null))
                .isInstanceOf(GenerateTokenFailException.class);
    }

    @Test
    void shouldNotGenerateUserAccessTokenWithoutSessionId() {
        TokenService service = tokenServiceWithoutSessions();

        assertThatThrownBy(() -> service.generateToken(user(), PROJECT_ID, authConfig(), " "))
                .isInstanceOf(GenerateTokenFailException.class);
    }

    private static TokenService tokenServiceWithoutSessions() {
        return new TokenService("secret", "issuer", Duration.ofMinutes(15));
    }

    private static Account account() {
        return Account.builder()
                .id(ACCOUNT_ID)
                .email("account@email.com")
                .CPF(new CPF("12345678909"))
                .build();
    }

    private static User user() {
        return User.builder()
                .id(USER_ID)
                .projectId(PROJECT_ID)
                .email("user@email.com")
                .status(UserStatus.ACTIVE)
                .build();
    }

    private static AuthConfig authConfig() {
        return AuthConfig.builder()
                .projectId(PROJECT_ID)
                .accessTokenExpirationMinutes(20)
                .refreshTokenExpirationDays(7)
                .build();
    }

    private static class InMemoryAccountSessionRepository implements AccountSessionRepository {
        private final AccountSession session;

        private InMemoryAccountSessionRepository(AccountSession session) {
            this.session = session;
        }

        @Override
        public AccountSession save(AccountSession accountSession) {
            return accountSession;
        }

        @Override
        public Optional<AccountSession> findById(String id) {
            if (session != null && id.equals(session.getId())) {
                return Optional.of(session);
            }

            return Optional.empty();
        }

        @Override
        public List<AccountSession> findAllByAccountId(String accountId) {
            return List.of();
        }

        @Override
        public List<AccountSession> findAllByAccountIdAndRevokedAtIsNull(String accountId) {
            return List.of();
        }

        @Override
        public void revokeById(String id) {
        }

        @Override
        public void revokeAllByAccountId(String accountId) {
        }

        @Override
        public long countByAccountIdAndRevokedAtIsNull(String accountId) {
            return 0;
        }
    }

    private static class InMemoryUserSessionRepository implements UserSessionRepository {
        private final UserSession session;

        private InMemoryUserSessionRepository(UserSession session) {
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
        }

        @Override
        public void revokeAllByProjectIdAndUserId(String projectId, String userId) {
        }

        @Override
        public long countByProjectIdAndUserIdAndRevokedAtIsNull(String projectId, String userId) {
            return 0;
        }
    }
}
