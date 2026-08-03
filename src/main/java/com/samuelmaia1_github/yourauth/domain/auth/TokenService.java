package com.samuelmaia1_github.yourauth.domain.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.samuelmaia1_github.yourauth.domain.auth.exceptions.GenerateTokenFailException;
import com.samuelmaia1_github.yourauth.domain.user.User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {
    @Value("${api.security.access-token.secret}")
    private String secret;

    @Value("${api.security.access-token.issuer}")
    private String issuer;

    public String generateToken(User user) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);

            return JWT
                    .create()
                    .withIssuer(issuer)
                    .withSubject(user.getId())
                    .withClaim("email", user.getEmail())
                    .withClaim("CPF", user.getCPF().getValue())
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
        return LocalDateTime.now()
                .plusMinutes(60 * 6)
                .toInstant(ZoneOffset.of("-03:00"));
    }
}
