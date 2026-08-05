package com.samuelmaia1_github.yourauth.presentation.dto.auth;

import com.samuelmaia1_github.yourauth.presentation.dto.account.AccountResponseDTO;

public record LoginMobileResponseDTO(
        AccountResponseDTO account,
        String accessToken,
        String refreshToken
) {
}
