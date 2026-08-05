package com.samuelmaia1_github.yourauth.presentation.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.samuelmaia1_github.yourauth.presentation.dto.account.AccountResponseDTO;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LoginResponseDTO(
        AccountResponseDTO account,
        String token
) {
}
