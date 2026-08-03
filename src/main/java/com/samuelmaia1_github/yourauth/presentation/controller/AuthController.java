package com.samuelmaia1_github.yourauth.presentation.controller;

import com.samuelmaia1_github.yourauth.domain.auth.AuthService;
import com.samuelmaia1_github.yourauth.domain.refreshtoken.RefreshTokenService;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.LoginDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.LoginMobileResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.LoginResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.user.UserResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;
    private final RefreshTokenService refreshTokenService;

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

        String rawRefreshToken = refreshTokenService.createRefreshToken(user.id(), userAgent);

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", rawRefreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("None")
                .maxAge(Duration.ofDays(7))
                .build();

        ResponseCookie accessCookie = ResponseCookie.from("access-token", accessToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("None")
                .maxAge(Duration.ofHours(6))
                .build();

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

        String rawRefreshToken = refreshTokenService.createRefreshToken(user.id(), userAgent);

        return ResponseEntity
                .ok()
                .body(new LoginMobileResponseDTO(user, accessToken, rawRefreshToken));
    }
}
