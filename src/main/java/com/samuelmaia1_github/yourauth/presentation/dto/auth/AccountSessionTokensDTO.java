package com.samuelmaia1_github.yourauth.presentation.dto.auth;

import com.samuelmaia1_github.yourauth.presentation.dto.auth.user.TokenDTO;

public record AccountSessionTokensDTO(
        TokenDTO accessToken,
        TokenDTO refreshToken
) {
}
