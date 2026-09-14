package com.samuelmaia1_github.yourauth.auth.unit;

import com.samuelmaia1_github.yourauth.domain.account.Account;
import com.samuelmaia1_github.yourauth.domain.account.AccountRepository;
import com.samuelmaia1_github.yourauth.domain.accountsession.AccountSession;
import com.samuelmaia1_github.yourauth.domain.accountsession.AccountSessionRepository;
import com.samuelmaia1_github.yourauth.domain.auth.AccountAuthService;
import com.samuelmaia1_github.yourauth.domain.auth.TokenService;
import com.samuelmaia1_github.yourauth.domain.refreshtoken.AccountRefreshTokenService;
import com.samuelmaia1_github.yourauth.domain.shared.Address;
import com.samuelmaia1_github.yourauth.domain.shared.Phone;
import com.samuelmaia1_github.yourauth.domain.valueobjects.CPF;
import com.samuelmaia1_github.yourauth.infra.interfaces.IPasswordEncoder;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.AccountLoginSessionDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.AccountRefreshResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.AccountSessionTokensDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.LoginDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.user.TokenDTO;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AccountAuthServiceTest {
    private static final String ACCOUNT_ID = "account-id";
    private static final String SESSION_ID = "session-id";

    @Test
    void shouldCreateRefreshTokenUsingSavedSessionIdOnLogin() {
        RecordingAccountSessionRepository sessionRepository = new RecordingAccountSessionRepository(null);
        StubAccountRefreshTokenService refreshTokenService = new StubAccountRefreshTokenService();
        StubTokenService tokenService = new StubTokenService();
        AccountAuthService service = service(sessionRepository, refreshTokenService, tokenService);

        AccountLoginSessionDTO loginSession = service.login(
                new LoginDTO("raw-password", "account@email.com", null),
                "127.0.0.1",
                "user-agent",
                "device"
        );

        assertThat(loginSession.account().id()).isEqualTo(ACCOUNT_ID);
        assertThat(loginSession.accessToken().raw()).isEqualTo("access-token");
        assertThat(loginSession.refreshToken().raw()).isEqualTo("created-refresh-token");
        assertThat(refreshTokenService.createdAccountId).isEqualTo(ACCOUNT_ID);
        assertThat(refreshTokenService.createdSessionId).isEqualTo(SESSION_ID);
        assertThat(refreshTokenService.createdUserAgent).isEqualTo("user-agent");
        assertThat(tokenService.generatedSessionId).isEqualTo(SESSION_ID);
        assertThat(sessionRepository.session.getAccountId()).isEqualTo(ACCOUNT_ID);
        assertThat(sessionRepository.session.getIpAddress()).isEqualTo("127.0.0.1");
        assertThat(sessionRepository.session.getUserAgent()).isEqualTo("user-agent");
        assertThat(sessionRepository.session.getDeviceName()).isEqualTo("device");
    }

    @Test
    void shouldRefreshAccountSessionAndUpdateLastUsedAt() {
        Instant previousLastUsedAt = Instant.now().minus(Duration.ofHours(1));
        AccountSession session = AccountSession.builder()
                .id(SESSION_ID)
                .accountId(ACCOUNT_ID)
                .lastUsedAt(previousLastUsedAt)
                .build();
        RecordingAccountSessionRepository sessionRepository = new RecordingAccountSessionRepository(session);
        StubAccountRefreshTokenService refreshTokenService = new StubAccountRefreshTokenService();
        StubTokenService tokenService = new StubTokenService();
        AccountAuthService service = service(sessionRepository, refreshTokenService, tokenService);

        AccountSessionTokensDTO tokens = service.refreshAccountSession("current-refresh-token");

        assertThat(refreshTokenService.refreshedRawToken).isEqualTo("current-refresh-token");
        assertThat(tokens.accessToken().raw()).isEqualTo("access-token");
        assertThat(tokens.accessToken().duration()).isEqualTo(Duration.ofMinutes(15));
        assertThat(tokens.refreshToken().raw()).isEqualTo("new-refresh-token");
        assertThat(tokenService.generatedSessionId).isEqualTo(SESSION_ID);
        assertThat(sessionRepository.savedSession.getLastUsedAt()).isAfter(previousLastUsedAt);
        assertThat(sessionRepository.session.isRevoked()).isFalse();
    }

    @Test
    void shouldLogoutAccountSessionUsingRefreshToken() {
        StubAccountRefreshTokenService refreshTokenService = new StubAccountRefreshTokenService();
        AccountAuthService service = service(
                new RecordingAccountSessionRepository(null),
                refreshTokenService,
                new StubTokenService()
        );

        service.logoutAccountSession("current-refresh-token");

        assertThat(refreshTokenService.loggedOutRawToken).isEqualTo("current-refresh-token");
    }

    private AccountAuthService service(
            RecordingAccountSessionRepository sessionRepository,
            StubAccountRefreshTokenService refreshTokenService,
            StubTokenService tokenService
    ) {
        return new AccountAuthService(
                new StubAccountRepository(account()),
                sessionRepository,
                new MatchingPasswordEncoder(),
                tokenService,
                refreshTokenService
        );
    }

    private static Account account() {
        return Account.builder()
                .id(ACCOUNT_ID)
                .name("Account")
                .lastName("Test")
                .email("account@email.com")
                .password("encoded-password")
                .CPF(new CPF("12345678909"))
                .address(Address.builder()
                        .cep("01001000")
                        .street("Street")
                        .number("100")
                        .neighborhood("Center")
                        .city("Sao Paulo")
                        .state("SP")
                        .build())
                .phone(new Phone("11", "999999999"))
                .build();
    }

    private static class StubAccountRefreshTokenService extends AccountRefreshTokenService {
        private String createdAccountId;
        private String createdSessionId;
        private String createdUserAgent;
        private String refreshedRawToken;
        private String loggedOutRawToken;

        private StubAccountRefreshTokenService() {
            super(null, null, null, null, Duration.ofDays(30));
        }

        @Override
        public TokenDTO createAccountRefreshToken(String accountId, String sessionId, String userAgent) {
            createdAccountId = accountId;
            createdSessionId = sessionId;
            createdUserAgent = userAgent;

            return new TokenDTO("created-refresh-token", Duration.ofDays(30));
        }

        @Override
        public AccountRefreshResponseDTO refresh(String currentRawToken) {
            refreshedRawToken = currentRawToken;

            return new AccountRefreshResponseDTO(
                    ACCOUNT_ID,
                    SESSION_ID,
                    new TokenDTO("new-refresh-token", Duration.ofDays(30))
            );
        }

        @Override
        public void logout(String currentRawToken) {
            loggedOutRawToken = currentRawToken;
        }
    }

    private static class StubTokenService extends TokenService {
        private String generatedSessionId;

        private StubTokenService() {
            super("secret", "issuer", Duration.ofMinutes(15));
        }

        @Override
        public String generateToken(Account account, String sessionId) {
            generatedSessionId = sessionId;
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

    private static class StubAccountRepository implements AccountRepository {
        private final Account account;

        private StubAccountRepository(Account account) {
            this.account = account;
        }

        @Override
        public Account save(Account account) {
            return account;
        }

        @Override
        public Optional<Account> findById(String id) {
            if (ACCOUNT_ID.equals(id)) {
                return Optional.of(account);
            }

            return Optional.empty();
        }

        @Override
        public Optional<Account> findByEmail(String email) {
            if ("account@email.com".equals(email)) {
                return Optional.of(account);
            }

            return Optional.empty();
        }

        @Override
        public Optional<Account> findByEmailIgnoreCase(String email) {
            return findByEmail(email);
        }

        @Override
        public Optional<Account> findByCPF(CPF cpf) {
            return Optional.empty();
        }

        @Override
        public Optional<Account> findByEmailIgnoreCaseOrCPF(String email, CPF cpf) {
            return Optional.empty();
        }

        @Override
        public void deleteById(String id) {
        }
    }

    private static class RecordingAccountSessionRepository implements AccountSessionRepository {
        private AccountSession session;
        private AccountSession savedSession;

        private RecordingAccountSessionRepository(AccountSession session) {
            this.session = session;
        }

        @Override
        public AccountSession save(AccountSession accountSession) {
            savedSession = accountSession;

            if (accountSession.getId() != null) {
                session = accountSession;
                return accountSession;
            }

            session = AccountSession.builder()
                    .id(SESSION_ID)
                    .accountId(accountSession.getAccountId())
                    .deviceName(accountSession.getDeviceName())
                    .ipAddress(accountSession.getIpAddress())
                    .userAgent(accountSession.getUserAgent())
                    .lastUsedAt(accountSession.getLastUsedAt())
                    .build();

            return session;
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
            if (session != null && id.equals(session.getId())) {
                session.revoke();
            }
        }

        @Override
        public void revokeAllByAccountId(String accountId) {
        }

        @Override
        public long countByAccountIdAndRevokedAtIsNull(String accountId) {
            return 0;
        }
    }
}
