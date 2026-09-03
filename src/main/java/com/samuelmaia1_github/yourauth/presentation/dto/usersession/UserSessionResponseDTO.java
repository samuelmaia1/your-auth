package com.samuelmaia1_github.yourauth.presentation.dto.usersession;

import com.samuelmaia1_github.yourauth.domain.usersession.UserSessionStatus;
import com.samuelmaia1_github.yourauth.presentation.dto.user.UserResponseDTO;

import java.time.Instant;

public record UserSessionResponseDTO(
        String id,
        String projectId,
        String userId,
        UserResponseDTO user,
        String deviceName,
        String ipAddress,
        String userAgent,
        Instant createdAt,
        Instant lastUsedAt,
        Instant revokedAt,
        UserSessionStatus status
) {
}
