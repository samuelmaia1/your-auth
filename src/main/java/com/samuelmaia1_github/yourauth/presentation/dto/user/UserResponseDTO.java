package com.samuelmaia1_github.yourauth.presentation.dto.user;

import com.samuelmaia1_github.yourauth.domain.user.UserStatus;
import com.samuelmaia1_github.yourauth.presentation.dto.shared.PhoneDTO;

import java.time.LocalDateTime;

public record UserResponseDTO(
        String id,
        String projectId,
        String email,
        UserStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime lastLoginAt,
        LocalDateTime lastPasswordChangedAt,
        LocalDateTime lastFailedLoginAt,
        Integer failedLoginAttempts,
        LocalDateTime lockedUntil,
        String lastLoginIpAddress,
        String lastLoginUserAgent,
        PhoneDTO phone
) {
}
