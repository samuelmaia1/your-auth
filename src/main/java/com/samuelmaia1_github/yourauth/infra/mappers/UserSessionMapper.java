package com.samuelmaia1_github.yourauth.infra.mappers;

import com.samuelmaia1_github.yourauth.domain.usersession.UserSession;
import com.samuelmaia1_github.yourauth.infra.repository.entity.UserSessionEntity;

public class UserSessionMapper {
    private UserSessionMapper() {
    }

    public static UserSession toDomain(UserSessionEntity entity) {
        if (entity == null) {
            return null;
        }

        return UserSession.builder()
                .id(entity.getId())
                .projectId(entity.getProjectId())
                .userId(entity.getUserId())
                .deviceName(entity.getDeviceName())
                .ipAddress(entity.getIpAddress())
                .userAgent(entity.getUserAgent())
                .createdAt(entity.getCreatedAt())
                .lastUsedAt(entity.getLastUsedAt())
                .revokedAt(entity.getRevokedAt())
                .version(entity.getVersion())
                .build();
    }

    public static UserSessionEntity toEntity(UserSession userSession) {
        if (userSession == null) {
            return null;
        }

        return UserSessionEntity.builder()
                .id(userSession.getId())
                .projectId(userSession.getProjectId())
                .userId(userSession.getUserId())
                .deviceName(userSession.getDeviceName())
                .ipAddress(userSession.getIpAddress())
                .userAgent(userSession.getUserAgent())
                .createdAt(userSession.getCreatedAt())
                .lastUsedAt(userSession.getLastUsedAt())
                .revokedAt(userSession.getRevokedAt())
                .version(userSession.getVersion())
                .build();
    }
}
