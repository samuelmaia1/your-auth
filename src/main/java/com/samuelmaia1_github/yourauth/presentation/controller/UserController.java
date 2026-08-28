package com.samuelmaia1_github.yourauth.presentation.controller;

import com.samuelmaia1_github.yourauth.domain.auth.AuthenticatedProjectApiKey;
import com.samuelmaia1_github.yourauth.domain.auth.UserAuthService;
import com.samuelmaia1_github.yourauth.domain.user.User;
import com.samuelmaia1_github.yourauth.domain.user.UserService;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.user.TokenDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.user.UserLoginDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.user.UserLoginResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.user.UserLoginSessionDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.user.UserTokensResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.user.CreateUserDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.user.UserResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.mapper.UserPresentationMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final UserAuthService userAuthService;

    @PostMapping
    public ResponseEntity<UserResponseDTO> create(
            @AuthenticationPrincipal AuthenticatedProjectApiKey authenticatedApiKey,
            @Valid @RequestBody CreateUserDTO dto
    ) {
        User user = UserPresentationMapper.toDomain(dto, authenticatedApiKey.projectId());
        User createdUser = userService.createWithApiKey(user, authenticatedApiKey.scopes());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(UserPresentationMapper.toResponseDTO(createdUser));
    }

    @PostMapping("/login")
    public ResponseEntity<UserLoginResponseDTO> login(
            @Valid @RequestBody UserLoginDTO credentials,
            @AuthenticationPrincipal AuthenticatedProjectApiKey authenticatedApiKey,
            @RequestHeader(
                    value = "X-Forwarded-User-Agent",
                    required = false
            ) String userAgent,
            @RequestHeader(
                    value = "X-End-User-IP",
                    required = false
            ) String ipAddress,
            @RequestHeader(
                    value = "X-Device-Name",
                    required = false
            ) String deviceName
    ) {
        UserLoginSessionDTO loginSession = userAuthService.
                login(credentials, authenticatedApiKey.projectId(), ipAddress, userAgent, deviceName);

        UserLoginResponseDTO responseDTO = loginSession.response();
        TokenDTO accessToken = responseDTO.token();

        TokenDTO refreshToken = loginSession.refreshToken();

        ResponseCookie refreshCookie = buildRefreshCookie(refreshToken);
        ResponseCookie accessCookie = buildAccessCookie(accessToken);

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .body(responseDTO);
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(
            @CookieValue("refresh_token") String refreshToken
    ) {
        UserTokensResponseDTO tokens = userAuthService.refreshUserSession(refreshToken);

        ResponseCookie refreshCookie = buildRefreshCookie(tokens.refreshToken());
        ResponseCookie accessCookie = buildAccessCookie(tokens.accessToken());

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue("refresh_token") String refreshToken
    ) {
        userAuthService.logoutUserSession(refreshToken);

        return ResponseEntity
                .noContent()
                .header(HttpHeaders.SET_COOKIE, clearCookie("refresh_token").toString())
                .header(HttpHeaders.SET_COOKIE, clearCookie("access-token").toString())
                .build();
    }

    private ResponseCookie buildRefreshCookie(TokenDTO refreshToken) {
        return ResponseCookie.from("refresh_token", refreshToken.raw())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("None")
                .maxAge(refreshToken.duration())
                .build();
    }

    private ResponseCookie buildAccessCookie(TokenDTO accessToken) {
        return ResponseCookie.from("access-token", accessToken.raw())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("None")
                .maxAge(accessToken.duration())
                .build();
    }

    private ResponseCookie clearCookie(String name) {
        return ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("None")
                .maxAge(Duration.ZERO)
                .build();
    }
}
