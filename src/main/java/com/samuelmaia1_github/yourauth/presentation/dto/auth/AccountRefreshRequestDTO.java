package com.samuelmaia1_github.yourauth.presentation.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record AccountRefreshRequestDTO(@NotBlank String refreshToken) {
}
