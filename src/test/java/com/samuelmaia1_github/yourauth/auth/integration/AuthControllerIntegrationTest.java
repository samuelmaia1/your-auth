package com.samuelmaia1_github.yourauth.auth.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.samuelmaia1_github.yourauth.domain.social.SocialLoginService;
import com.samuelmaia1_github.yourauth.domain.social.SocialIdentity;
import com.samuelmaia1_github.yourauth.domain.social.SocialIdentityRepository;
import com.samuelmaia1_github.yourauth.domain.social.SocialProvider;
import com.samuelmaia1_github.yourauth.domain.social.SocialProviderProfile;
import com.samuelmaia1_github.yourauth.domain.social.exceptions.SocialIdentityConflictException;
import com.samuelmaia1_github.yourauth.infra.interfaces.IPasswordEncoder;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:auth_controller_integration_test",
        "spring.cache.type=simple",
        "spring.data.redis.host=localhost",
        "spring.data.redis.port=6379",
        "spring.data.redis.password="
})
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private IPasswordEncoder passwordEncoder;

    @Autowired
    private SocialLoginService socialLoginService;

    @Autowired
    private SocialIdentityRepository socialIdentityRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldExchangeSocialCodeCreatingAccountSessionRefreshTokenAndCookies() throws Exception {
        String code = socialLoginService.createOneTimeCode(new SocialProviderProfile(
                SocialProvider.GOOGLE,
                "google-sub",
                "Social@Example.com",
                true,
                "Social",
                null,
                "https://cdn.example.com/avatar.png"
        ));

        MvcResult exchangeResult = mockMvc.perform(post("/auth/social/exchange")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("User-Agent", "test-agent")
                        .header("X-End-User-IP", "203.0.113.10")
                        .header("X-Device-Name", "Test Device")
                        .content("""
                                {
                                  "code": "%s"
                                }
                                """.formatted(code)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(exchangeResult.getResponse().getContentAsString());
        String accountId = body.get("id").asText();

        assertThat(body.get("email").asText()).isEqualTo("social@example.com");
        assertThat(body.get("avatarUrl").asText()).isEqualTo("https://cdn.example.com/avatar.png");
        assertThat(body.get("profileComplete").asBoolean()).isFalse();
        assertThat(body.has("accessToken")).isFalse();
        assertThat(body.has("refreshToken")).isFalse();
        assertThat(body.get("CPF").isNull()).isTrue();
        assertThat(body.get("lastName").isNull()).isTrue();
        assertThat(body.get("address").isNull()).isTrue();
        assertThat(body.get("phone").isNull()).isTrue();

        List<String> cookies = exchangeResult.getResponse().getHeaders(HttpHeaders.SET_COOKIE);
        assertThat(cookies).anySatisfy(cookie -> assertThat(cookie)
                .contains("refresh_token=")
                .contains("HttpOnly")
                .contains("Secure")
                .contains("SameSite=None"));
        assertThat(cookies).anySatisfy(cookie -> assertThat(cookie)
                .contains("access-token=")
                .contains("HttpOnly")
                .contains("Secure")
                .contains("SameSite=None"));

        assertThat(activeSessions(accountId)).isEqualTo(1);
        assertThat(refreshTokens(accountId)).isEqualTo(1);
        assertThat(socialIdentities(accountId)).isEqualTo(1);
        assertThat(accountField(accountId, "password")).isNull();
        assertThat(accountField(accountId, "cpf")).isNull();
        assertThat(accountField(accountId, "phone_ddd")).isNull();
        assertThat(accountField(accountId, "address_postal_code")).isNull();
        assertThat(accountField(accountId, "avatar_url")).isEqualTo("https://cdn.example.com/avatar.png");

        mockMvc.perform(post("/auth/social/exchange")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "%s"
                                }
                                """.formatted(code)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReadCurrentAccountAfterSocialExchangeWithoutJSessionId() throws Exception {
        MvcResult exchangeResult = exchangeSocialCode(SocialProvider.GOOGLE, "google-me-sub", "social-me@example.com");
        JsonNode body = objectMapper.readTree(exchangeResult.getResponse().getContentAsString());
        Cookie accessTokenCookie = responseCookie(exchangeResult, "access-token");

        mockMvc.perform(get("/accounts/me")
                        .cookie(accessTokenCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(body.get("id").asText()))
                .andExpect(jsonPath("$.email").value("social-me@example.com"));
    }

    @Test
    void shouldReadCurrentAccountAfterSocialExchangeEvenWhenOAuthSessionIsRestored() throws Exception {
        MvcResult exchangeResult = exchangeSocialCode(SocialProvider.GOOGLE, "google-oauth-session-sub", "social-oauth-session@example.com");
        JsonNode body = objectMapper.readTree(exchangeResult.getResponse().getContentAsString());
        Cookie accessTokenCookie = responseCookie(exchangeResult, "access-token");
        MockHttpSession oauthSession = oauthSession();

        mockMvc.perform(get("/accounts/me")
                        .session(oauthSession)
                        .cookie(accessTokenCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(body.get("id").asText()))
                .andExpect(jsonPath("$.email").value("social-oauth-session@example.com"));
    }

    @Test
    void shouldReadCurrentAccountAfterAccountRefresh() throws Exception {
        MvcResult exchangeResult = exchangeSocialCode(SocialProvider.GOOGLE, "google-refresh-me-sub", "social-refresh-me@example.com");
        Cookie refreshTokenCookie = responseCookie(exchangeResult, "refresh_token");

        MvcResult refreshResult = mockMvc.perform(post("/auth/refresh")
                        .cookie(refreshTokenCookie))
                .andExpect(status().isOk())
                .andReturn();

        mockMvc.perform(get("/accounts/me")
                        .cookie(responseCookie(refreshResult, "access-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("social-refresh-me@example.com"));
    }

    @Test
    void shouldReadCurrentAccountAfterTraditionalLogin() throws Exception {
        insertAccount("traditional-me-account", "traditional-me@example.com", "45678912300", "raw-password");

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("User-Agent", "test-agent")
                        .header("X-End-User-IP", "203.0.113.10")
                        .header("X-Device-Name", "Test Device")
                        .content("""
                                {
                                  "email": "traditional-me@example.com",
                                  "password": "raw-password"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        mockMvc.perform(get("/accounts/me")
                        .cookie(responseCookie(loginResult, "access-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("traditional-me-account"))
                .andExpect(jsonPath("$.email").value("traditional-me@example.com"));
    }

    @Test
    void shouldInvalidateCurrentAccountEndpointAfterLogout() throws Exception {
        insertAccount("logout-me-account", "logout-me@example.com", "45678912301", "raw-password");

        MvcResult loginResult = mockMvc.perform(post("/auth/mobile/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("User-Agent", "test-agent")
                        .header("X-End-User-IP", "203.0.113.10")
                        .header("X-Device-Name", "Test Device")
                        .content("""
                                {
                                  "email": "logout-me@example.com",
                                  "password": "raw-password"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode loginBody = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String accessToken = loginBody.get("accessToken").asText();
        String refreshToken = loginBody.get("refreshToken").asText();

        mockMvc.perform(get("/accounts/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("logout-me-account"));

        mockMvc.perform(post("/auth/mobile/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "%s"
                                }
                                """.formatted(refreshToken)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/accounts/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldExchangeGithubSocialCodeAndReadCurrentAccount() throws Exception {
        MvcResult exchangeResult = exchangeSocialCode(SocialProvider.GITHUB, "github-me-sub", "github-me@example.com");

        mockMvc.perform(get("/accounts/me")
                        .cookie(responseCookie(exchangeResult, "access-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("github-me@example.com"));
    }

    @Test
    void shouldKeepFirstSocialAvatarWhenLinkingAnotherProviderWithSameEmail() throws Exception {
        MvcResult githubResult = exchangeSocialCode(
                SocialProvider.GITHUB,
                "github-avatar-sub",
                "social-avatar-link@example.com",
                "https://cdn.example.com/github-avatar.png"
        );
        JsonNode githubBody = objectMapper.readTree(githubResult.getResponse().getContentAsString());
        String accountId = githubBody.get("id").asText();

        MvcResult googleResult = exchangeSocialCode(
                SocialProvider.GOOGLE,
                "google-avatar-sub",
                "social-avatar-link@example.com",
                "https://cdn.example.com/google-avatar.png"
        );
        JsonNode googleBody = objectMapper.readTree(googleResult.getResponse().getContentAsString());

        assertThat(googleBody.get("id").asText()).isEqualTo(accountId);
        assertThat(googleBody.get("avatarUrl").asText()).isEqualTo("https://cdn.example.com/github-avatar.png");
        assertThat(accountField(accountId, "avatar_url")).isEqualTo("https://cdn.example.com/github-avatar.png");
        assertThat(socialIdentities(accountId)).isEqualTo(2);
    }

    @Test
    void shouldEnforceUniqueSocialIdentityConstraint() {
        socialLoginService.createOneTimeCode(new SocialProviderProfile(
                SocialProvider.GITHUB,
                "github-id",
                "constraint@example.com",
                true,
                "Constraint",
                null,
                null
        ));

        String accountId = jdbcTemplate.queryForObject(
                "SELECT id FROM accounts WHERE email = ?",
                String.class,
                "constraint@example.com"
        );

        assertThatThrownBy(() -> socialIdentityRepository.save(SocialIdentity.builder()
                .accountId(accountId)
                .provider(SocialProvider.GITHUB)
                .providerUserId("another-github-id")
                .providerEmail("constraint@example.com")
                .build()))
                .isInstanceOf(SocialIdentityConflictException.class);
    }

    @Test
    void shouldInvalidateAccountAccessTokenWhenSessionIsRevokedThroughLogout() throws Exception {
        insertAccount("account-id", "account-auth@example.com", "12345678909", "raw-password");

        MvcResult loginResult = mockMvc.perform(post("/auth/mobile/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("User-Agent", "test-agent")
                        .header("X-End-User-IP", "203.0.113.10")
                        .header("X-Device-Name", "Test Device")
                        .content("""
                                {
                                  "email": "account-auth@example.com",
                                  "password": "raw-password"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode loginBody = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String accessToken = loginBody.get("accessToken").asText();
        String refreshToken = loginBody.get("refreshToken").asText();

        assertThat(activeSessions("account-id")).isEqualTo(1);

        mockMvc.perform(get("/accounts")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("email", "account-auth@example.com"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/auth/mobile/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "%s"
                                }
                                """.formatted(refreshToken)))
                .andExpect(status().isNoContent());

        assertThat(activeSessions("account-id")).isZero();

        mockMvc.perform(get("/accounts")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("email", "account-auth@example.com"))
                .andExpect(status().isUnauthorized());
    }

    private MvcResult exchangeSocialCode(
            SocialProvider provider,
            String providerUserId,
            String email
    ) throws Exception {
        return exchangeSocialCode(provider, providerUserId, email, "https://cdn.example.com/avatar.png");
    }

    private MvcResult exchangeSocialCode(
            SocialProvider provider,
            String providerUserId,
            String email,
            String avatarUrl
    ) throws Exception {
        String code = socialLoginService.createOneTimeCode(new SocialProviderProfile(
                provider,
                providerUserId,
                email,
                true,
                "Social",
                null,
                avatarUrl
        ));

        return mockMvc.perform(post("/auth/social/exchange")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("User-Agent", "test-agent")
                        .header("X-End-User-IP", "203.0.113.10")
                        .header("X-Device-Name", "Test Device")
                        .content("""
                                {
                                  "code": "%s"
                                }
                                """.formatted(code)))
                .andExpect(status().isOk())
                .andReturn();
    }

    private Cookie responseCookie(MvcResult result, String name) {
        return new Cookie(name, responseCookieValue(result, name));
    }

    private String responseCookieValue(MvcResult result, String name) {
        String prefix = name + "=";
        String header = result.getResponse()
                .getHeaders(HttpHeaders.SET_COOKIE)
                .stream()
                .filter(cookie -> cookie.startsWith(prefix))
                .findFirst()
                .orElseThrow();
        int end = header.indexOf(';');

        if (end == -1) {
            return header.substring(prefix.length());
        }

        return header.substring(prefix.length(), end);
    }

    private MockHttpSession oauthSession() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(oauthAuthentication());

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        return session;
    }

    private OAuth2AuthenticationToken oauthAuthentication() {
        DefaultOAuth2User principal = new DefaultOAuth2User(
                List.of(),
                Map.of("sub", "github-sub"),
                "sub"
        );

        return new OAuth2AuthenticationToken(principal, List.of(), "github");
    }

    private Integer activeSessions(String accountId) {
        return jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM account_sessions
                        WHERE account_id = ?
                          AND revoked_at IS NULL
                        """,
                Integer.class,
                accountId
        );
    }

    private Integer refreshTokens(String accountId) {
        return jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM account_refresh_tokens
                        WHERE account_id = ?
                        """,
                Integer.class,
                accountId
        );
    }

    private Integer socialIdentities(String accountId) {
        return jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM social_identities
                        WHERE account_id = ?
                        """,
                Integer.class,
                accountId
        );
    }

    private String accountField(String accountId, String column) {
        return jdbcTemplate.queryForObject(
                "SELECT " + column + " FROM accounts WHERE id = ?",
                String.class,
                accountId
        );
    }

    private void insertAccount(String id, String email, String cpf, String password) {
        jdbcTemplate.update("""
                INSERT INTO accounts (
                    id,
                    email,
                    password,
                    cpf,
                    name,
                    last_name,
                    phone_ddd,
                    phone_number,
                    address_postal_code,
                    address_street,
                    address_number,
                    address_neighborhood,
                    address_city,
                    address_state
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                id,
                email,
                passwordEncoder.encode(password),
                cpf,
                "Auth",
                "Account",
                "11",
                "999999999",
                "01001000",
                "Street",
                "100",
                "Center",
                "Sao Paulo",
                "SP"
        );
    }
}
