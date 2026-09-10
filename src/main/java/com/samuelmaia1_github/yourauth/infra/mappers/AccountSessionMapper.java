package com.samuelmaia1_github.yourauth.infra.mappers;

import com.samuelmaia1_github.yourauth.domain.accountsession.AccountSession;
import com.samuelmaia1_github.yourauth.infra.repository.entity.AccountSessionEntity;

public class AccountSessionMapper {
    private AccountSessionMapper() {
    }

    public static AccountSession toDomain(AccountSessionEntity entity) {
        if (entity == null) {
            return null;
        }

        return AccountSession.builder()
                .id(entity.getId())
                .accountId(entity.getAccountId())
                .deviceName(entity.getDeviceName())
                .ipAddress(entity.getIpAddress())
                .userAgent(entity.getUserAgent())
                .createdAt(entity.getCreatedAt())
                .lastUsedAt(entity.getLastUsedAt())
                .revokedAt(entity.getRevokedAt())
                .version(entity.getVersion())
                .build();
    }

    public static AccountSessionEntity toEntity(AccountSession accountSession) {
        if (accountSession == null) {
            return null;
        }

        return AccountSessionEntity.builder()
                .id(accountSession.getId())
                .accountId(accountSession.getAccountId())
                .deviceName(accountSession.getDeviceName())
                .ipAddress(accountSession.getIpAddress())
                .userAgent(accountSession.getUserAgent())
                .createdAt(accountSession.getCreatedAt())
                .lastUsedAt(accountSession.getLastUsedAt())
                .revokedAt(accountSession.getRevokedAt())
                .version(accountSession.getVersion())
                .build();
    }
}
