package com.samuelmaia1_github.yourauth.presentation.dto.auth;

import com.samuelmaia1_github.yourauth.presentation.dto.user.UserResponseDTO;

public record LoginMobileResponseDTO(
        UserResponseDTO user,
        String accessToken,
        String refreshToken
) {
}
