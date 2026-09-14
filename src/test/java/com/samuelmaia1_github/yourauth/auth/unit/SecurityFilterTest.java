package com.samuelmaia1_github.yourauth.auth.unit;

import com.samuelmaia1_github.yourauth.domain.account.Account;
import com.samuelmaia1_github.yourauth.domain.accountsession.AccountSession;
import com.samuelmaia1_github.yourauth.domain.accountsession.AccountSessionRepository;
import com.samuelmaia1_github.yourauth.domain.auth.AuthenticatedAccount;
import com.samuelmaia1_github.yourauth.domain.auth.AuthenticatedProjectApiKey;
import com.samuelmaia1_github.yourauth.domain.auth.TokenService;
import com.samuelmaia1_github.yourauth.domain.project.authconfig.AuthConfig;
import com.samuelmaia1_github.yourauth.domain.user.User;
import com.samuelmaia1_github.yourauth.domain.user.UserStatus;
import com.samuelmaia1_github.yourauth.domain.usersession.UserSession;
import com.samuelmaia1_github.yourauth.domain.usersession.UserSessionRepository;
import com.samuelmaia1_github.yourauth.domain.valueobjects.CPF;
import com.samuelmaia1_github.yourauth.infra.security.SecurityFilter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityFilterTest {
    private static final String ACCOUNT_ID = "account-id";
    private static final String ACCOUNT_EMAIL = "account@example.com";
    private static final String ACCOUNT_SESSION_ID = "account-session-id";
    private static final String PROJECT_ID = "project-id";
    private static final String USER_ID = "user-id";
    private static final String USER_SESSION_ID = "user-session-id";

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAuthenticateAccountWhenThereIsNoExistingAuthenticationAndJwtIsValid() throws Exception {
        TokenService tokenService = tokenService(activeAccountSession(), null);
        SecurityFilter filter = new SecurityFilter(tokenService);

        performFilter(filter, requestWithAccessToken(accountToken(tokenService)));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isInstanceOf(UsernamePasswordAuthenticationToken.class);
        assertThat(authentication.getPrincipal()).isInstanceOf(AuthenticatedAccount.class);

        AuthenticatedAccount principal = (AuthenticatedAccount) authentication.getPrincipal();
        assertThat(principal.id()).isEqualTo(ACCOUNT_ID);
        assertThat(principal.email()).isEqualTo(ACCOUNT_EMAIL);
        assertThat(principal.sessionId()).isEqualTo(ACCOUNT_SESSION_ID);
    }

    @Test
    void shouldReplaceOAuth2AuthenticationWhenAccountJwtIsValid() throws Exception {
        TokenService tokenService = tokenService(activeAccountSession(), null);
        SecurityFilter filter = new SecurityFilter(tokenService);
        OAuth2AuthenticationToken oauthAuthentication = oauthAuthentication();
        SecurityContextHolder.getContext().setAuthentication(oauthAuthentication);

        performFilter(filter, requestWithAccessToken(accountToken(tokenService)));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotSameAs(oauthAuthentication);
        assertThat(authentication).isInstanceOf(UsernamePasswordAuthenticationToken.class);
        assertThat(authentication.getPrincipal()).isInstanceOf(AuthenticatedAccount.class);
        assertThat(((AuthenticatedAccount) authentication.getPrincipal()).id()).isEqualTo(ACCOUNT_ID);
    }

    @Test
    void shouldNotRecreateAuthenticationWhenAlreadyAuthenticatedAsAccount() throws Exception {
        TokenService tokenService = tokenService(activeAccountSession(), null);
        SecurityFilter filter = new SecurityFilter(tokenService);
        AuthenticatedAccount principal = new AuthenticatedAccount("existing-account", "existing@example.com", "existing-session");
        Authentication existingAuthentication = new UsernamePasswordAuthenticationToken(principal, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(existingAuthentication);

        performFilter(filter, requestWithAccessToken(accountToken(tokenService)));

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(existingAuthentication);
    }

    @Test
    void shouldNotCreateJwtAuthenticationWhenOAuth2AuthenticationExistsAndThereIsNoAccessToken() throws Exception {
        SecurityFilter filter = new SecurityFilter(tokenService(activeAccountSession(), null));
        OAuth2AuthenticationToken oauthAuthentication = oauthAuthentication();
        SecurityContextHolder.getContext().setAuthentication(oauthAuthentication);

        performFilter(filter, request());

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(oauthAuthentication);
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .isNotInstanceOf(AuthenticatedAccount.class);
    }

    @Test
    void shouldNotCreateJwtAuthenticationWhenOAuth2AuthenticationExistsAndJwtIsInvalid() throws Exception {
        SecurityFilter filter = new SecurityFilter(tokenService(activeAccountSession(), null));
        OAuth2AuthenticationToken oauthAuthentication = oauthAuthentication();
        SecurityContextHolder.getContext().setAuthentication(oauthAuthentication);

        performFilter(filter, requestWithAccessToken("invalid-jwt"));

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(oauthAuthentication);
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .isNotInstanceOf(AuthenticatedAccount.class);
    }

    @Test
    void shouldNotAcceptUserTokenInAccountFilter() throws Exception {
        UserSession userSession = UserSession.builder()
                .id(USER_SESSION_ID)
                .projectId(PROJECT_ID)
                .userId(USER_ID)
                .build();
        TokenService tokenService = tokenService(null, userSession);
        SecurityFilter filter = new SecurityFilter(tokenService);

        performFilter(filter, requestWithAccessToken(userToken(tokenService)));

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void shouldNotAuthenticateAccountWhenAccountSessionIsRevoked() throws Exception {
        AccountSession session = activeAccountSession();
        session.revoke();
        TokenService tokenService = tokenService(session, null);
        SecurityFilter filter = new SecurityFilter(tokenService);

        performFilter(filter, requestWithAccessToken(accountToken(tokenService)));

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void shouldPreserveProjectApiKeyAuthenticationWhenAccountJwtIsAlsoPresent() throws Exception {
        TokenService tokenService = tokenService(activeAccountSession(), null);
        SecurityFilter filter = new SecurityFilter(tokenService);
        Authentication apiKeyAuthentication = new UsernamePasswordAuthenticationToken(
                new AuthenticatedProjectApiKey("api-key-auth", PROJECT_ID, "key-id", Set.of()),
                null,
                List.of()
        );
        SecurityContextHolder.getContext().setAuthentication(apiKeyAuthentication);

        performFilter(filter, requestWithAccessToken(accountToken(tokenService)));

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(apiKeyAuthentication);
    }

    private void performFilter(SecurityFilter filter, MockHttpServletRequest request) throws ServletException, IOException {
        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());
    }

    private MockHttpServletRequest requestWithAccessToken(String token) {
        MockHttpServletRequest request = request();
        request.setCookies(new Cookie("access-token", token));
        return request;
    }

    private MockHttpServletRequest request() {
        return new MockHttpServletRequest("GET", "/accounts/me");
    }

    private String accountToken(TokenService tokenService) {
        return tokenService.generateToken(account(), ACCOUNT_SESSION_ID);
    }

    private String userToken(TokenService tokenService) {
        return tokenService.generateToken(user(), PROJECT_ID, authConfig(), USER_SESSION_ID);
    }

    private TokenService tokenService(AccountSession accountSession, UserSession userSession) {
        return new TokenService(
                "secret",
                "issuer",
                Duration.ofMinutes(15),
                new InMemoryAccountSessionRepository(accountSession),
                new InMemoryUserSessionRepository(userSession)
        );
    }

    private AccountSession activeAccountSession() {
        return AccountSession.builder()
                .id(ACCOUNT_SESSION_ID)
                .accountId(ACCOUNT_ID)
                .build();
    }

    private Account account() {
        return Account.builder()
                .id(ACCOUNT_ID)
                .email(ACCOUNT_EMAIL)
                .CPF(new CPF("12345678909"))
                .build();
    }

    private User user() {
        return User.builder()
                .id(USER_ID)
                .projectId(PROJECT_ID)
                .email("user@example.com")
                .status(UserStatus.ACTIVE)
                .build();
    }

    private AuthConfig authConfig() {
        return AuthConfig.builder()
                .projectId(PROJECT_ID)
                .accessTokenExpirationMinutes(20)
                .refreshTokenExpirationDays(7)
                .build();
    }

    private OAuth2AuthenticationToken oauthAuthentication() {
        DefaultOAuth2User principal = new DefaultOAuth2User(
                List.of(),
                java.util.Map.of("sub", "github-sub"),
                "sub"
        );

        return new OAuth2AuthenticationToken(principal, List.of(), "github");
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
