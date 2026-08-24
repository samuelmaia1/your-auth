package com.samuelmaia1_github.yourauth.infra.mappers;

import com.samuelmaia1_github.yourauth.domain.user.User;
import com.samuelmaia1_github.yourauth.infra.repository.entity.UserEntity;

public class UserMapper {
    private UserMapper() {
    }

    public static User toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }

        return User.builder()
                .id(entity.getId())
                .projectId(entity.getProjectId())
                .email(entity.getEmail())
                .password(entity.getPassword())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .lastLoginAt(entity.getLastLoginAt())
                .lastPasswordChangedAt(entity.getLastPasswordChangedAt())
                .lastFailedLoginAt(entity.getLastFailedLoginAt())
                .failedLoginAttempts(entity.getFailedLoginAttempts())
                .lockedUntil(entity.getLockedUntil())
                .lastLoginIpAddress(entity.getLastLoginIpAddress())
                .lastLoginUserAgent(entity.getLastLoginUserAgent())
                .phone(entity.getPhone())
                .build();
    }

    public static UserEntity toEntity(User user) {
        if (user == null) {
            return null;
        }

        return UserEntity.builder()
                .id(user.getId())
                .projectId(user.getProjectId())
                .email(user.getEmail())
                .password(user.getPassword())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .lastPasswordChangedAt(user.getLastPasswordChangedAt())
                .lastFailedLoginAt(user.getLastFailedLoginAt())
                .failedLoginAttempts(user.getFailedLoginAttempts())
                .lockedUntil(user.getLockedUntil())
                .lastLoginIpAddress(user.getLastLoginIpAddress())
                .lastLoginUserAgent(user.getLastLoginUserAgent())
                .phone(user.getPhone())
                .build();
    }
}
