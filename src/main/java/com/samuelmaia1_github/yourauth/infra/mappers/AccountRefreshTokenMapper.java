package com.samuelmaia1_github.yourauth.infra.mappers;

import com.samuelmaia1_github.yourauth.domain.refreshtoken.AccountRefreshToken;
import com.samuelmaia1_github.yourauth.infra.repository.entity.AccountRefreshTokenEntity;

public class AccountRefreshTokenMapper {
    private AccountRefreshTokenMapper() {
    }

    public static AccountRefreshToken toDomain(AccountRefreshTokenEntity entity) {
        if (entity == null) {
            return null;
        }

        return AccountRefreshToken.builder()
                .id(entity.getId())
                .accountId(entity.getAccountId())
                .hash(entity.getHash())
                .familyId(entity.getFamilyId())
                .expiresAt(entity.getExpiresAt())
                .revokedAt(entity.getRevokedAt())
                .createdAt(entity.getCreatedAt())
                .userAgent(entity.getUserAgent())
                .version(entity.getVersion())
                .build();
    }

    public static AccountRefreshTokenEntity toEntity(AccountRefreshToken refreshToken) {
        if (refreshToken == null) {
            return null;
        }

        return AccountRefreshTokenEntity.builder()
                .id(refreshToken.getId())
                .accountId(refreshToken.getAccountId())
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
