package com.samuelmaia1_github.yourauth.presentation.dto.auth.user;

import com.samuelmaia1_github.yourauth.presentation.dto.user.UserResponseDTO;

import java.time.LocalDateTime;

public record UserLoginResponseDTO(
        UserResponseDTO user,
        TokenDTO token,
        Boolean success,
        LocalDateTime loggedAt
) {
}
