package com.samuelmaia1_github.yourauth.presentation.dto.projectapikey;

public record CreatedProjectApiKeyResponseDTO(
        String key,
        ProjectApiKeyResponseDTO apiKey
) {
}
