package com.samuelmaia1_github.yourauth.presentation.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;

final class AccessTokenResolver {
    private static final String ACCESS_TOKEN_COOKIE = "access-token";
    private static final String API_KEY_PREFIX = "ya_sk_";
    private static final String BEARER_PREFIX = "Bearer ";

    private AccessTokenResolver() {
    }

    static String resolve(HttpServletRequest request) {
        String bearerToken = bearerToken(request);

        if (bearerToken != null) {
            return bearerToken;
        }

        return accessTokenCookie(request);
    }

    private static String bearerToken(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return null;
        }

        String token = authHeader.substring(BEARER_PREFIX.length()).trim();

        if (token.isBlank() || token.startsWith(API_KEY_PREFIX)) {
            return null;
        }

        return token;
    }

    private static String accessTokenCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            String value = cookie.getValue();

            if (ACCESS_TOKEN_COOKIE.equals(cookie.getName()) && value != null && !value.isBlank()) {
                return value;
            }
        }

        return null;
    }
}
