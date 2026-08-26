package com.samuelmaia1_github.yourauth.presentation.dto.auth.user;

public record UserRefreshResponseDTO(
        String projectId,
        String userId,
        String rawRefreshToken
) {
}
