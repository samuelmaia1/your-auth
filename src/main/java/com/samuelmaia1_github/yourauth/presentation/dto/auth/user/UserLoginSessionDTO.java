package com.samuelmaia1_github.yourauth.presentation.dto.auth.user;

public record UserLoginSessionDTO(
        UserLoginResponseDTO response,
        TokenDTO refreshToken
) {
}
