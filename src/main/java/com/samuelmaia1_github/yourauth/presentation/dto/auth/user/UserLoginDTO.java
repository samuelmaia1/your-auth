package com.samuelmaia1_github.yourauth.presentation.dto.auth.user;

import jakarta.validation.constraints.NotBlank;

public record UserLoginDTO(
        @NotBlank
        String email,

        @NotBlank
        String password
) {
}
