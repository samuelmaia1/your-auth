package com.samuelmaia1_github.yourauth.presentation.dto.auth;

import com.samuelmaia1_github.yourauth.presentation.dto.account.AccountResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.user.TokenDTO;

public record AccountLoginSessionDTO(
        AccountResponseDTO account,
        TokenDTO accessToken,
        TokenDTO refreshToken
) {
}
