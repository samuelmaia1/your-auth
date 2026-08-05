package com.samuelmaia1_github.yourauth.domain.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.samuelmaia1_github.yourauth.domain.auth.exceptions.GenerateTokenFailException;
import com.samuelmaia1_github.yourauth.domain.account.Account;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class TokenService {
    private final String secret;
    private final String issuer;
    private final Duration accessTokenDuration;

    public TokenService(
            @Value("${api.security.access-token.secret}") String secret,
            @Value("${api.security.access-token.issuer}") String issuer,
            @Value("${api.security.access-token.duration}") Duration accessTokenDuration
    ) {
        this.secret = secret;
        this.issuer = issuer;
        this.accessTokenDuration = accessTokenDuration;
    }

    public String generateToken(Account account) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);

            return JWT
                    .create()
                    .withIssuer(issuer)
                    .withSubject(account.getId())
                    .withClaim("email", account.getEmail())
                    .withClaim("CPF", account.getCPF().getValue())
                    .withExpiresAt(generateExpirationDate())
                    .sign(algorithm);
        } catch (Exception exception) {
            throw new GenerateTokenFailException("Falha ao gerar token de acesso");
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

    private DecodedJWT decode(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);

            return JWT.require(algorithm)
                    .withIssuer(issuer)
                    .build()
                    .verify(token);

        } catch (JWTVerificationException exception) {
            throw new JWTVerificationException("Token JWT inválido ou expirado");
        }
    }

    private Instant generateExpirationDate() {
        return Instant.now().plus(accessTokenDuration);
    }
}
