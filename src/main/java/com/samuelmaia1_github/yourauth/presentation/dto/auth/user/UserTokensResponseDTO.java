package com.samuelmaia1_github.yourauth.presentation.dto.auth.user;

public record UserTokensResponseDTO(
        TokenDTO accessToken,
        TokenDTO refreshToken
) {
}
