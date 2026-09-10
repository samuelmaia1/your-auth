package com.samuelmaia1_github.yourauth.domain.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.samuelmaia1_github.yourauth.domain.auth.exceptions.GenerateTokenFailException;
import com.samuelmaia1_github.yourauth.domain.account.Account;
import com.samuelmaia1_github.yourauth.domain.accountsession.AccountSession;
import com.samuelmaia1_github.yourauth.domain.accountsession.AccountSessionRepository;
import com.samuelmaia1_github.yourauth.domain.project.authconfig.AuthConfig;
import com.samuelmaia1_github.yourauth.domain.user.User;
import com.samuelmaia1_github.yourauth.domain.usersession.UserSession;
import com.samuelmaia1_github.yourauth.domain.usersession.UserSessionRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

@Service
public class TokenService {
    private static final String TOKEN_TYPE_CLAIM = "type";
    private static final String SESSION_ID_CLAIM = "sessionId";
    private static final String PROJECT_ID_CLAIM = "projectId";
    private static final String ACCOUNT_TOKEN_TYPE = "ACCOUNT";
    private static final String USER_TOKEN_TYPE = "USER";

    private final String secret;
    private final String issuer;
    private final Duration accessTokenDuration;
    private final AccountSessionRepository accountSessionRepository;
    private final UserSessionRepository userSessionRepository;

    @Autowired
    public TokenService(
            @Value("${api.security.access-token.secret}") String secret,
            @Value("${api.security.access-token.issuer}") String issuer,
            @Value("${api.security.access-token.duration}") Duration accessTokenDuration,
            AccountSessionRepository accountSessionRepository,
            UserSessionRepository userSessionRepository
    ) {
        this.secret = secret;
        this.issuer = issuer;
        this.accessTokenDuration = accessTokenDuration;
        this.accountSessionRepository = accountSessionRepository;
        this.userSessionRepository = userSessionRepository;
    }

    public TokenService(String secret, String issuer, Duration accessTokenDuration) {
        this.secret = secret;
        this.issuer = issuer;
        this.accessTokenDuration = accessTokenDuration;
        this.accountSessionRepository = null;
        this.userSessionRepository = null;
    }

    public String generateToken(Account account, String sessionId) {
        try {
            ensureSessionIdIsPresent(sessionId);
            Algorithm algorithm = Algorithm.HMAC256(secret);

            return JWT
                    .create()
                    .withIssuer(issuer)
                    .withSubject(account.getId())
                    .withClaim(TOKEN_TYPE_CLAIM, ACCOUNT_TOKEN_TYPE)
                    .withClaim("email", account.getEmail())
                    .withClaim("CPF", account.getCPF().getValue())
                    .withClaim(SESSION_ID_CLAIM, sessionId)
                    .withExpiresAt(generateExpirationDate())
                    .sign(algorithm);
        } catch (Exception exception) {
            throw new GenerateTokenFailException("Falha ao gerar token de acesso", exception);
        }
    }

    public String generateToken(User user, String projectId, AuthConfig config, String sessionId) {
        try {
            ensureSessionIdIsPresent(sessionId);
            Algorithm algorithm = Algorithm.HMAC256(secret);

            Duration duration = Duration.ofMinutes(config.getAccessTokenExpirationMinutes());

            return JWT
                    .create()
                    .withIssuer(issuer)
                    .withSubject(user.getId())
                    .withClaim(TOKEN_TYPE_CLAIM, USER_TOKEN_TYPE)
                    .withClaim("email", user.getEmail())
                    .withClaim(PROJECT_ID_CLAIM, projectId)
                    .withClaim(SESSION_ID_CLAIM, sessionId)
                    .withExpiresAt(generateExpirationDate(duration))
                    .sign(algorithm);
        } catch (Exception exception) {
            throw new GenerateTokenFailException("Falha ao gerar token de acesso", exception);
        }
    }

    public boolean isValid(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);

            JWT.require(algorithm)
                    .withIssuer(issuer)
                    .build()
                    .verify(token);

            return true;
        } catch (JWTVerificationException exception) {
            return false;
        }
    }

    public boolean isValidAccountAccessToken(String token) {
        try {
            DecodedJWT decodedToken = verify(token);

            return hasExpectedType(decodedToken, ACCOUNT_TOKEN_TYPE)
                    && hasValidAccountSession(decodedToken);
        } catch (JWTVerificationException exception) {
            return false;
        }
    }

    public boolean isValidUserAccessToken(String token) {
        try {
            DecodedJWT decodedToken = verify(token);

            return hasExpectedType(decodedToken, USER_TOKEN_TYPE)
                    && hasValidUserSession(decodedToken);
        } catch (JWTVerificationException exception) {
            return false;
        }
    }

    public String getSubject(String token) {
        return decode(token).getSubject();
    }

    public String recoverToken(HttpServletRequest request) {
        var authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer "))
            return authHeader.replace("Bearer ", "");

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("access-token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }

    public String getEmail(String token) {
        return decode(token).getClaim("email").asString();
    }

    public String getSessionId(String token) {
        return decode(token).getClaim(SESSION_ID_CLAIM).asString();
    }

    public String getProjectId(String token) {
        return decode(token).getClaim(PROJECT_ID_CLAIM).asString();
    }

    public Duration getAccessTokenDuration() {
        return accessTokenDuration;
    }

    private DecodedJWT decode(String token) {
        try {
            return verify(token);

        } catch (JWTVerificationException exception) {
            throw new JWTVerificationException("Token JWT inválido ou expirado");
        }
    }

    private DecodedJWT verify(String token) {
        Algorithm algorithm = Algorithm.HMAC256(secret);

        return JWT.require(algorithm)
                .withIssuer(issuer)
                .build()
                .verify(token);
    }

    private boolean hasExpectedType(DecodedJWT token, String expectedType) {
        return expectedType.equals(token.getClaim(TOKEN_TYPE_CLAIM).asString());
    }

    private void ensureSessionIdIsPresent(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("Session id não informado");
        }
    }

    private boolean hasValidAccountSession(DecodedJWT token) {
        if (accountSessionRepository == null) {
            return false;
        }

        String sessionId = token.getClaim(SESSION_ID_CLAIM).asString();
        String accountId = token.getSubject();

        if (sessionId == null || sessionId.isBlank()) {
            return false;
        }

        return accountSessionRepository.findById(sessionId)
                .filter(AccountSession::isValid)
                .filter(session -> Objects.equals(accountId, session.getAccountId()))
                .isPresent();
    }

    private boolean hasValidUserSession(DecodedJWT token) {
        if (userSessionRepository == null) {
            return false;
        }

        String sessionId = token.getClaim(SESSION_ID_CLAIM).asString();
        String projectId = token.getClaim(PROJECT_ID_CLAIM).asString();
        String userId = token.getSubject();

        if (sessionId == null || sessionId.isBlank()) {
            return false;
        }

        return userSessionRepository.findById(sessionId)
                .filter(UserSession::isValid)
                .filter(session -> Objects.equals(projectId, session.getProjectId()))
                .filter(session -> Objects.equals(userId, session.getUserId()))
                .isPresent();
    }

    private Instant generateExpirationDate() {
        return Instant.now().plus(accessTokenDuration);
    }

    private Instant generateExpirationDate(Duration duration) {
        return Instant.now().plus(duration);
    }
}
