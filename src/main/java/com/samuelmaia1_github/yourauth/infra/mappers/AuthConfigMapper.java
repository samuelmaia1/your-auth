package com.samuelmaia1_github.yourauth.infra.mappers;

import com.samuelmaia1_github.yourauth.domain.project.authconfig.AuthConfig;
import com.samuelmaia1_github.yourauth.infra.repository.entity.AuthConfigEntity;

public class AuthConfigMapper {
    private AuthConfigMapper() {
    }

    public static AuthConfig toDomain(AuthConfigEntity entity) {
        if (entity == null) {
            return null;
        }

        return AuthConfig.builder()
                .id(entity.getId())
                .projectId(entity.getProjectId())
                .accessTokenExpirationMinutes(entity.getAccessTokenExpirationMinutes())
                .refreshTokenExpirationDays(entity.getRefreshTokenExpirationDays())
                .sessionMode(entity.getSessionMode())
                .maxActiveSessions(entity.getMaxActiveSessions())
                .refreshTokenRotationEnabled(entity.isRefreshTokenRotationEnabled())
                .revokeTokensOnPasswordChange(entity.isRevokeTokensOnPasswordChange())
                .failedLoginAttemptsLimit(entity.getFailedLoginAttemptsLimit())
                .lockDurationMinutes(entity.getLockDurationMinutes())
                .requireEmailVerification(entity.isRequireEmailVerification())
                .registrationEnabled(entity.isRegistrationEnabled())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static AuthConfigEntity toEntity(AuthConfig config) {
        if (config == null) {
            return null;
        }

        return AuthConfigEntity.builder()
                .id(config.getId())
                .projectId(config.getProjectId())
                .accessTokenExpirationMinutes(config.getAccessTokenExpirationMinutes())
                .refreshTokenExpirationDays(config.getRefreshTokenExpirationDays())
                .sessionMode(config.getSessionMode())
                .maxActiveSessions(config.getMaxActiveSessions())
                .refreshTokenRotationEnabled(config.isRefreshTokenRotationEnabled())
                .revokeTokensOnPasswordChange(config.isRevokeTokensOnPasswordChange())
                .failedLoginAttemptsLimit(config.getFailedLoginAttemptsLimit())
                .lockDurationMinutes(config.getLockDurationMinutes())
                .requireEmailVerification(config.isRequireEmailVerification())
                .registrationEnabled(config.isRegistrationEnabled())
                .createdAt(config.getCreatedAt())
                .updatedAt(config.getUpdatedAt())
                .build();
    }
}
