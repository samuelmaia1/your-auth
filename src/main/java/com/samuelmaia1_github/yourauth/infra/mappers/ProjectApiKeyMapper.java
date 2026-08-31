package com.samuelmaia1_github.yourauth.infra.mappers;

import com.samuelmaia1_github.yourauth.domain.projectapikey.ProjectApiKey;
import com.samuelmaia1_github.yourauth.infra.repository.entity.ProjectApiKeyEntity;

import java.util.HashSet;

public class ProjectApiKeyMapper {
    private ProjectApiKeyMapper() {
    }

    public static ProjectApiKey toDomain(ProjectApiKeyEntity entity) {
        if (entity == null) {
            return null;
        }

        return ProjectApiKey.builder()
                .id(entity.getId())
                .projectId(entity.getProjectId())
                .name(entity.getName())
                .keyId(entity.getKeyId())
                .prefix(entity.getPrefix())
                .secretHash(entity.getSecretHash())
                .secretLastFour(entity.getSecretLastFour())
                .environment(entity.getEnvironment())
                .scopes(entity.getScopes() == null ? new HashSet<>() : new HashSet<>(entity.getScopes()))
                .createdByAccountId(entity.getCreatedByAccountId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .lastUsedAt(entity.getLastUsedAt())
                .revokedAt(entity.getRevokedAt())
                .expiresAt(entity.getExpiresAt())
                .build();
    }

    public static ProjectApiKeyEntity toEntity(ProjectApiKey apiKey) {
        if (apiKey == null) {
            return null;
        }

        return ProjectApiKeyEntity.builder()
                .id(apiKey.getId())
                .projectId(apiKey.getProjectId())
                .name(apiKey.getName())
                .keyId(apiKey.getKeyId())
                .prefix(apiKey.getPrefix())
                .secretHash(apiKey.getSecretHash())
                .secretLastFour(apiKey.getSecretLastFour())
                .environment(apiKey.getEnvironment())
                .scopes(apiKey.getScopes() == null ? new HashSet<>() : new HashSet<>(apiKey.getScopes()))
                .createdByAccountId(apiKey.getCreatedByAccountId())
                .createdAt(apiKey.getCreatedAt())
                .updatedAt(apiKey.getUpdatedAt())
                .lastUsedAt(apiKey.getLastUsedAt())
                .revokedAt(apiKey.getRevokedAt())
                .expiresAt(apiKey.getExpiresAt())
                .build();
    }
}
