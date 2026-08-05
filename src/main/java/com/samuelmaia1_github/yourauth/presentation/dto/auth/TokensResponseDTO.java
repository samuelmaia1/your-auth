package com.samuelmaia1_github.yourauth.presentation.dto.auth;

import lombok.Builder;

@Builder
public record TokensResponseDTO(String accessToken, String refreshToken) {
}
