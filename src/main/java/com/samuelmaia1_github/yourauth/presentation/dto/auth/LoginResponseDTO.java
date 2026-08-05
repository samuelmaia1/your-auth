package com.samuelmaia1_github.yourauth.presentation.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.samuelmaia1_github.yourauth.presentation.dto.user.UserResponseDTO;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LoginResponseDTO(
        UserResponseDTO user,
        String token
) {
}
