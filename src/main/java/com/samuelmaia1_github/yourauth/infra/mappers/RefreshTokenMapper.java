package com.samuelmaia1_github.yourauth.infra.mappers;

import com.samuelmaia1_github.yourauth.domain.refreshtoken.RefreshToken;
import com.samuelmaia1_github.yourauth.infra.repository.entity.RefreshTokenEntity;

public class RefreshTokenMapper {
    private RefreshTokenMapper() {
    }

    public static RefreshToken toDomain(RefreshTokenEntity entity) {
        if (entity == null) {
            return null;
        }

        return RefreshToken.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .hash(entity.getHash())
                .familyId(entity.getFamilyId())
                .expiresAt(entity.getExpiresAt())
                .revokedAt(entity.getRevokedAt())
                .createdAt(entity.getCreatedAt())
                .userAgent(entity.getUserAgent())
                .version(entity.getVersion())
                .build();
    }

    public static RefreshTokenEntity toEntity(RefreshToken refreshToken) {
        if (refreshToken == null) {
            return null;
        }

        return RefreshTokenEntity.builder()
                .id(refreshToken.getId())
                .userId(refreshToken.getUserId())
                .hash(refreshToken.getHash())
                .familyId(refreshToken.getFamilyId())
                .expiresAt(refreshToken.getExpiresAt())
                .revokedAt(refreshToken.getRevokedAt())
                .createdAt(refreshToken.getCreatedAt())
                .userAgent(refreshToken.getUserAgent())
                .version(refreshToken.getVersion())
                .build();
    }
}
