package com.samuelmaia1_github.yourauth.presentation.controller;

import com.samuelmaia1_github.yourauth.domain.auth.AuthService;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.*;
import com.samuelmaia1_github.yourauth.presentation.dto.user.UserResponseDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService service;
    private final Duration refreshTokenDuration;
    private final Duration accessTokenDuration;

    public AuthController(
            AuthService service,
            @Value("${api.security.refresh-token.duration}") Duration refreshTokenDuration,
            @Value("${api.security.access-token.duration}") Duration accessTokenDuration
    ) {
        this.service = service;
        this.refreshTokenDuration = refreshTokenDuration;
        this.accessTokenDuration = accessTokenDuration;
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponseDTO> login(
            @Valid @RequestBody LoginDTO loginDTO,
            @RequestHeader(
                    value = HttpHeaders.USER_AGENT,
                    required = false
            ) String userAgent
    ) {
        LoginResponseDTO loginData = service.login(loginDTO);

        UserResponseDTO user = loginData.user();
        String accessToken = loginData.token();

        String rawRefreshToken = service.generateRefreshToken(user.id(), userAgent);

        ResponseCookie refreshCookie = buildRefreshCookie(rawRefreshToken);
        ResponseCookie accessCookie = buildAccessCookie(accessToken);

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .body(user);
    }

    @PostMapping("/mobile/login")
    public ResponseEntity<LoginMobileResponseDTO> mobileLogin(
            @Valid @RequestBody LoginDTO loginDTO,
            @RequestHeader(
                    value = HttpHeaders.USER_AGENT,
                    required = false
            ) String userAgent
    ) {
        LoginResponseDTO loginData = service.login(loginDTO);

        UserResponseDTO user = loginData.user();
        String accessToken = loginData.token();

        String rawRefreshToken = service.generateRefreshToken(user.id(), userAgent);

        return ResponseEntity
                .ok()
                .body(new LoginMobileResponseDTO(user, accessToken, rawRefreshToken));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokensResponseDTO> refreshToken(
            @CookieValue("refresh_token") String refreshToken
    ) {
        TokensResponseDTO tokens = service.refreshSession(refreshToken);

        ResponseCookie refreshCookie = buildRefreshCookie(tokens.refreshToken());
        ResponseCookie accessCookie = buildAccessCookie(tokens.accessToken());

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .body(tokens);
    }

    @PostMapping("/mobile/refresh")
    public ResponseEntity<TokensResponseDTO> refreshMobileToken(
            @Valid @RequestBody RefreshRequestDTO requestDTO
    ) {
        TokensResponseDTO tokens = service.refreshSession(requestDTO.refreshToken());

        return ResponseEntity
                .ok()
                .body(tokens);
    }

    private ResponseCookie buildRefreshCookie(String refreshToken) {
        return ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("None")
                .maxAge(refreshTokenDuration)
                .build();
    }

    private ResponseCookie buildAccessCookie(String accessToken) {
        return ResponseCookie.from("access-token", accessToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("None")
                .maxAge(accessTokenDuration)
                .build();
    }
}
