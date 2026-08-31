package com.samuelmaia1_github.yourauth.presentation.dto.auth;

public record AccountRefreshResponseDTO(
        String accountId,
        String rawRefreshToken
) {
}
