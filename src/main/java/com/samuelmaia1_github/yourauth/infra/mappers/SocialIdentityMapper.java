package com.samuelmaia1_github.yourauth.infra.mappers;

import com.samuelmaia1_github.yourauth.domain.social.SocialIdentity;
import com.samuelmaia1_github.yourauth.infra.repository.entity.SocialIdentityEntity;

public class SocialIdentityMapper {
    private SocialIdentityMapper() {
    }

    public static SocialIdentity toDomain(SocialIdentityEntity entity) {
        if (entity == null) {
            return null;
        }

        return SocialIdentity.builder()
                .id(entity.getId())
                .accountId(entity.getAccountId())
                .provider(entity.getProvider())
                .providerUserId(entity.getProviderUserId())
                .providerEmail(entity.getProviderEmail())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static SocialIdentityEntity toEntity(SocialIdentity identity) {
        if (identity == null) {
            return null;
        }

        return SocialIdentityEntity.builder()
                .id(identity.getId())
                .accountId(identity.getAccountId())
                .provider(identity.getProvider())
                .providerUserId(identity.getProviderUserId())
                .providerEmail(identity.getProviderEmail())
                .createdAt(identity.getCreatedAt())
                .updatedAt(identity.getUpdatedAt())
                .build();
    }
}
