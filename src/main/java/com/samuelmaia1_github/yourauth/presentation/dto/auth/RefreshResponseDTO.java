package com.samuelmaia1_github.yourauth.presentation.dto.auth;

import lombok.Builder;

@Builder
public record RefreshResponseDTO(
        String accountId,
        String rawRefreshToken
) {
}
