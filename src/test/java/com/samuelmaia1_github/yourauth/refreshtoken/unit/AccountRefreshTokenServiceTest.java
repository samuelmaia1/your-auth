package com.samuelmaia1_github.yourauth.refreshtoken.unit;

import com.samuelmaia1_github.yourauth.domain.accountsession.AccountSession;
import com.samuelmaia1_github.yourauth.domain.accountsession.AccountSessionRepository;
import com.samuelmaia1_github.yourauth.domain.auth.exceptions.InvalidTokenException;
import com.samuelmaia1_github.yourauth.domain.refreshtoken.AccountRefreshToken;
import com.samuelmaia1_github.yourauth.domain.refreshtoken.AccountRefreshTokenRepository;
import com.samuelmaia1_github.yourauth.domain.refreshtoken.AccountRefreshTokenService;
import com.samuelmaia1_github.yourauth.domain.refreshtoken.RefreshTokenGenerator;
import com.samuelmaia1_github.yourauth.domain.refreshtoken.RefreshTokenHasher;
import com.samuelmaia1_github.yourauth.domain.refreshtoken.exceptions.RefreshTokenReuseException;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.AccountRefreshResponseDTO;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountRefreshTokenServiceTest {
    private static final String ACCOUNT_ID = "account-id";
    private static final String SESSION_ID = "session-id";

    @Test
    void shouldRotateRefreshTokenKeepingCurrentSession() {
        RefreshTokenHasher hasher = new RefreshTokenHasher("test-secret");
        InMemoryAccountRefreshTokenRepository refreshTokenRepository = new InMemoryAccountRefreshTokenRepository();
        RecordingAccountSessionRepository sessionRepository = new RecordingAccountSessionRepository(activeSession());
        AccountRefreshToken currentToken = currentToken(hasher, null);
        refreshTokenRepository.save(currentToken);
        refreshTokenRepository.savedTokens.clear();
        AccountRefreshTokenService service = service(
                hasher,
                new FixedRefreshTokenGenerator("new-refresh-token"),
                refreshTokenRepository,
                sessionRepository
        );

        AccountRefreshResponseDTO response = service.refresh("current-refresh-token");

        assertThat(response.accountId()).isEqualTo(ACCOUNT_ID);
        assertThat(response.sessionId()).isEqualTo(SESSION_ID);
        assertThat(response.refreshToken().raw()).isEqualTo("new-refresh-token");
        assertThat(response.refreshToken().duration()).isEqualTo(Duration.ofDays(30));
        assertThat(refreshTokenRepository.savedTokens).hasSize(2);
        assertThat(currentToken.isRevoked()).isTrue();

        AccountRefreshToken newToken = refreshTokenRepository
                .findByHash(hasher.hash("new-refresh-token"))
                .orElseThrow();
        assertThat(newToken.getSessionId()).isEqualTo(SESSION_ID);
        assertThat(newToken.getAccountId()).isEqualTo(ACCOUNT_ID);
        assertThat(newToken.isValid()).isTrue();
        assertThat(sessionRepository.revokedSessionId).isNull();
    }

    @Test
    void shouldRevokeSessionWhenRefreshTokenIsReused() {
        RefreshTokenHasher hasher = new RefreshTokenHasher("test-secret");
        InMemoryAccountRefreshTokenRepository refreshTokenRepository = new InMemoryAccountRefreshTokenRepository();
        RecordingAccountSessionRepository sessionRepository = new RecordingAccountSessionRepository(activeSession());
        refreshTokenRepository.save(currentToken(hasher, Instant.now()));
        AccountRefreshTokenService service = service(
                hasher,
                new FixedRefreshTokenGenerator("new-refresh-token"),
                refreshTokenRepository,
                sessionRepository
        );

        assertThatThrownBy(() -> service.refresh("current-refresh-token"))
                .isInstanceOf(RefreshTokenReuseException.class)
                .hasMessage("Refresh token reutilizado. A sessão foi encerrada.");

        assertThat(refreshTokenRepository.revokedSessionId).isEqualTo(SESSION_ID);
        assertThat(sessionRepository.revokedSessionId).isEqualTo(SESSION_ID);
    }

    @Test
    void shouldRevokeRefreshTokensWhenSessionDoesNotExist() {
        RefreshTokenHasher hasher = new RefreshTokenHasher("test-secret");
        InMemoryAccountRefreshTokenRepository refreshTokenRepository = new InMemoryAccountRefreshTokenRepository();
        RecordingAccountSessionRepository sessionRepository = new RecordingAccountSessionRepository(null);
        refreshTokenRepository.save(currentToken(hasher, null));
        AccountRefreshTokenService service = service(
                hasher,
                new FixedRefreshTokenGenerator("new-refresh-token"),
                refreshTokenRepository,
                sessionRepository
        );

        assertThatThrownBy(() -> service.refresh("current-refresh-token"))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("Sessão inválida ou expirada.");

        assertThat(refreshTokenRepository.revokedSessionId).isEqualTo(SESSION_ID);
        assertThat(sessionRepository.revokedSessionId).isEqualTo(SESSION_ID);
    }

    @Test
    void shouldRevokeSessionAndCurrentRefreshTokensOnLogout() {
        RefreshTokenHasher hasher = new RefreshTokenHasher("test-secret");
        InMemoryAccountRefreshTokenRepository refreshTokenRepository = new InMemoryAccountRefreshTokenRepository();
        RecordingAccountSessionRepository sessionRepository = new RecordingAccountSessionRepository(activeSession());
        refreshTokenRepository.save(currentToken(hasher, null));
        AccountRefreshTokenService service = service(
                hasher,
                new FixedRefreshTokenGenerator("new-refresh-token"),
                refreshTokenRepository,
                sessionRepository
        );

        service.logout("current-refresh-token");

        assertThat(refreshTokenRepository.revokedSessionId).isEqualTo(SESSION_ID);
        assertThat(sessionRepository.revokedSessionId).isEqualTo(SESSION_ID);
        assertThat(refreshTokenRepository.findByHash(hasher.hash("current-refresh-token")).orElseThrow().isRevoked())
                .isTrue();
    }

    @Test
    void shouldRejectBlankRefreshToken() {
        AccountRefreshTokenService service = service(
                new RefreshTokenHasher("test-secret"),
                new FixedRefreshTokenGenerator("new-refresh-token"),
                new InMemoryAccountRefreshTokenRepository(),
                new RecordingAccountSessionRepository(activeSession())
        );

        assertThatThrownBy(() -> service.refresh(" "))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("Refresh token não informado");
    }

    private AccountRefreshTokenService service(
            RefreshTokenHasher hasher,
            RefreshTokenGenerator generator,
            AccountRefreshTokenRepository refreshTokenRepository,
            AccountSessionRepository sessionRepository
    ) {
        return new AccountRefreshTokenService(
                hasher,
                generator,
                refreshTokenRepository,
                sessionRepository,
                Duration.ofDays(30)
        );
    }

    private AccountRefreshToken currentToken(RefreshTokenHasher hasher, Instant revokedAt) {
        return AccountRefreshToken.builder()
                .id("refresh-token-id")
                .accountId(ACCOUNT_ID)
                .hash(hasher.hash("current-refresh-token"))
                .sessionId(SESSION_ID)
                .expiresAt(Instant.now().plus(Duration.ofDays(1)))
                .revokedAt(revokedAt)
                .userAgent("user-agent")
                .build();
    }

    private static AccountSession activeSession() {
        return AccountSession.builder()
                .id(SESSION_ID)
                .accountId(ACCOUNT_ID)
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

    private static class InMemoryAccountRefreshTokenRepository implements AccountRefreshTokenRepository {
        private final List<AccountRefreshToken> tokens = new ArrayList<>();
        private final List<AccountRefreshToken> savedTokens = new ArrayList<>();
        private String revokedSessionId;

        @Override
        public AccountRefreshToken save(AccountRefreshToken refreshToken) {
            tokens.removeIf(token -> token.getHash().equals(refreshToken.getHash()));
            tokens.add(refreshToken);
            savedTokens.add(refreshToken);
            return refreshToken;
        }

        @Override
        public Optional<AccountRefreshToken> findById(String id) {
            return tokens.stream()
                    .filter(token -> id.equals(token.getId()))
                    .findFirst();
        }

        @Override
        public Optional<AccountRefreshToken> findByHash(String hash) {
            return tokens.stream()
                    .filter(token -> hash.equals(token.getHash()))
                    .findFirst();
        }

        @Override
        public List<AccountRefreshToken> findAllByAccountId(String accountId) {
            return tokens.stream()
                    .filter(token -> accountId.equals(token.getAccountId()))
                    .toList();
        }

        @Override
        public List<AccountRefreshToken> findAllBySessionId(String sessionId) {
            return tokens.stream()
                    .filter(token -> sessionId.equals(token.getSessionId()))
                    .toList();
        }

        @Override
        public void revokeSession(String sessionId) {
            revokedSessionId = sessionId;
            findAllBySessionId(sessionId).stream()
                    .filter(token -> !token.isRevoked())
                    .forEach(AccountRefreshToken::revoke);
        }

        @Override
        public void deleteById(String id) {
            tokens.removeIf(token -> id.equals(token.getId()));
        }
    }

    private static class RecordingAccountSessionRepository implements AccountSessionRepository {
        private final AccountSession session;
        private String revokedSessionId;

        private RecordingAccountSessionRepository(AccountSession session) {
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
            revokedSessionId = id;

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
