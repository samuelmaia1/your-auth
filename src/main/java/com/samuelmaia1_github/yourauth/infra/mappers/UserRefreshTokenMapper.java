package com.samuelmaia1_github.yourauth.infra.mappers;

import com.samuelmaia1_github.yourauth.domain.refreshtoken.UserRefreshToken;
import com.samuelmaia1_github.yourauth.infra.repository.entity.UserRefreshTokenEntity;

public class UserRefreshTokenMapper {
    private UserRefreshTokenMapper() {
    }

    public static UserRefreshToken toDomain(UserRefreshTokenEntity entity) {
        if (entity == null) {
            return null;
        }

        return UserRefreshToken.builder()
                .id(entity.getId())
                .projectId(entity.getProjectId())
                .userId(entity.getUserId())
                .hash(entity.getHash())
                .sessionId(entity.getSessionId())
                .expiresAt(entity.getExpiresAt())
                .revokedAt(entity.getRevokedAt())
                .createdAt(entity.getCreatedAt())
                .userAgent(entity.getUserAgent())
                .version(entity.getVersion())
                .build();
    }

    public static UserRefreshTokenEntity toEntity(UserRefreshToken refreshToken) {
        if (refreshToken == null) {
            return null;
        }

        return UserRefreshTokenEntity.builder()
                .id(refreshToken.getId())
                .projectId(refreshToken.getProjectId())
                .userId(refreshToken.getUserId())
                .hash(refreshToken.getHash())
                .sessionId(refreshToken.getSessionId())
                .expiresAt(refreshToken.getExpiresAt())
                .revokedAt(refreshToken.getRevokedAt())
                .createdAt(refreshToken.getCreatedAt())
                .userAgent(refreshToken.getUserAgent())
                .version(refreshToken.getVersion())
                .build();
    }
}
