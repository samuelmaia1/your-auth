package com.samuelmaia1_github.yourauth.presentation.mapper;

import com.samuelmaia1_github.yourauth.domain.project.authconfig.AuthConfig;
import com.samuelmaia1_github.yourauth.presentation.dto.authconfig.AuthConfigDTO;

public class AuthConfigPresentationMapper {
    private AuthConfigPresentationMapper() {
    }

    public static AuthConfig toDomain(AuthConfigDTO dto) {
        if (dto == null) {
            return null;
        }

        return AuthConfig.builder()
                .accessTokenExpirationMinutes(
                        dto.accessTokenExpirationMinutes() != null
                                ? dto.accessTokenExpirationMinutes()
                                : AuthConfig.DEFAULT_ACCESS_TOKEN_EXPIRATION_MINUTES
                )
                .refreshTokenExpirationDays(
                        dto.refreshTokenExpirationDays() != null
                                ? dto.refreshTokenExpirationDays()
                                : AuthConfig.DEFAULT_REFRESH_TOKEN_EXPIRATION_DAYS
                )
                .sessionMode(
                        dto.sessionMode() != null
                                ? dto.sessionMode()
                                : AuthConfig.DEFAULT_SESSION_MODE
                )
                .maxActiveSessions(dto.maxActiveSessions())
                .refreshTokenRotationEnabled(
                        dto.refreshTokenRotationEnabled() != null
                                ? dto.refreshTokenRotationEnabled()
                                : AuthConfig.DEFAULT_REFRESH_TOKEN_ROTATION_ENABLED
                )
                .revokeTokensOnPasswordChange(
                        dto.revokeTokensOnPasswordChange() != null
                                ? dto.revokeTokensOnPasswordChange()
                                : AuthConfig.DEFAULT_REVOKE_TOKENS_ON_PASSWORD_CHANGE
                )
                .failedLoginAttemptsLimit(
                        dto.failedLoginAttemptsLimit() != null
                                ? dto.failedLoginAttemptsLimit()
                                : AuthConfig.DEFAULT_FAILED_LOGIN_ATTEMPTS_LIMIT
                )
                .lockDurationMinutes(
                        dto.lockDurationMinutes() != null
                                ? dto.lockDurationMinutes()
                                : AuthConfig.DEFAULT_LOCK_DURATION_MINUTES
                )
                .requireEmailVerification(
                        dto.requireEmailVerification() != null
                                ? dto.requireEmailVerification()
                                : AuthConfig.DEFAULT_REQUIRE_EMAIL_VERIFICATION
                )
                .registrationEnabled(
                        dto.registrationEnabled() != null
                                ? dto.registrationEnabled()
                                : AuthConfig.DEFAULT_REGISTRATION_ENABLED
                )
                .build();
    }

    public static AuthConfigDTO toDto(AuthConfig domain) {
        return new AuthConfigDTO(
                domain.getAccessTokenExpirationMinutes(),
                domain.getRefreshTokenExpirationDays(),
                domain.getSessionMode(),
                domain.getMaxActiveSessions(),
                domain.isRefreshTokenRotationEnabled(),
                domain.isRevokeTokensOnPasswordChange(),
                domain.getFailedLoginAttemptsLimit(),
                domain.getLockDurationMinutes(),
                domain.isRequireEmailVerification(),
                domain.isRegistrationEnabled()
        );
    }
}
