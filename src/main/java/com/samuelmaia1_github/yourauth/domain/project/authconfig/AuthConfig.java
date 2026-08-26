package com.samuelmaia1_github.yourauth.domain.project.authconfig;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class AuthConfig {
    public static final int DEFAULT_ACCESS_TOKEN_EXPIRATION_MINUTES = 15;
    public static final int DEFAULT_REFRESH_TOKEN_EXPIRATION_DAYS = 7;
    public static final SessionMode DEFAULT_SESSION_MODE = SessionMode.MULTIPLE_DEVICES;
    public static final boolean DEFAULT_REFRESH_TOKEN_ROTATION_ENABLED = true;
    public static final boolean DEFAULT_REVOKE_TOKENS_ON_PASSWORD_CHANGE = true;
    public static final int DEFAULT_FAILED_LOGIN_ATTEMPTS_LIMIT = 5;
    public static final int DEFAULT_LOCK_DURATION_MINUTES = 15;
    public static final boolean DEFAULT_REQUIRE_EMAIL_VERIFICATION = false;
    public static final boolean DEFAULT_REGISTRATION_ENABLED = true;

    private String id;
    private String projectId;
    private int accessTokenExpirationMinutes;
    private int refreshTokenExpirationDays;
    private SessionMode sessionMode;
    private Integer maxActiveSessions;
    private boolean refreshTokenRotationEnabled;
    private boolean revokeTokensOnPasswordChange;
    private int failedLoginAttemptsLimit;
    private int lockDurationMinutes;
    private boolean requireEmailVerification;
    private boolean registrationEnabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AuthConfig createDefault() {
        return AuthConfig.builder()
                .accessTokenExpirationMinutes(DEFAULT_ACCESS_TOKEN_EXPIRATION_MINUTES)
                .refreshTokenExpirationDays(DEFAULT_REFRESH_TOKEN_EXPIRATION_DAYS)
                .sessionMode(DEFAULT_SESSION_MODE)
                .maxActiveSessions(null)
                .refreshTokenRotationEnabled(DEFAULT_REFRESH_TOKEN_ROTATION_ENABLED)
                .revokeTokensOnPasswordChange(DEFAULT_REVOKE_TOKENS_ON_PASSWORD_CHANGE)
                .failedLoginAttemptsLimit(DEFAULT_FAILED_LOGIN_ATTEMPTS_LIMIT)
                .lockDurationMinutes(DEFAULT_LOCK_DURATION_MINUTES)
                .requireEmailVerification(DEFAULT_REQUIRE_EMAIL_VERIFICATION)
                .registrationEnabled(DEFAULT_REGISTRATION_ENABLED)
                .build();
    }

    public void assignToProject(String projectId) {
        if (projectId == null || projectId.isBlank()) {
            throw new IllegalArgumentException(
                    "O ID do projeto é obrigatório."
            );
        }

        if (this.projectId != null) {
            throw new IllegalStateException(
                    "A configuração já pertence a um projeto."
            );
        }

        this.projectId = projectId;
    }
}
