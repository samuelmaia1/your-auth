package com.samuelmaia1_github.yourauth.infra.mappers;

import com.samuelmaia1_github.yourauth.domain.social.SocialLoginCode;
import com.samuelmaia1_github.yourauth.infra.repository.entity.SocialLoginCodeEntity;

public class SocialLoginCodeMapper {
    private SocialLoginCodeMapper() {
    }

    public static SocialLoginCode toDomain(SocialLoginCodeEntity entity) {
        if (entity == null) {
            return null;
        }

        return SocialLoginCode.builder()
                .id(entity.getId())
                .accountId(entity.getAccountId())
                .hash(entity.getHash())
                .expiresAt(entity.getExpiresAt())
                .consumedAt(entity.getConsumedAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static SocialLoginCodeEntity toEntity(SocialLoginCode code) {
        if (code == null) {
            return null;
        }

        return SocialLoginCodeEntity.builder()
                .id(code.getId())
                .accountId(code.getAccountId())
                .hash(code.getHash())
                .expiresAt(code.getExpiresAt())
                .consumedAt(code.getConsumedAt())
                .createdAt(code.getCreatedAt())
                .build();
    }
}
