package com.samuelmaia1_github.yourauth.presentation.dto.auth;

import com.samuelmaia1_github.yourauth.presentation.dto.auth.user.TokenDTO;

public record AccountRefreshResponseDTO(
        String accountId,
        String sessionId,
        TokenDTO refreshToken
) {
}
