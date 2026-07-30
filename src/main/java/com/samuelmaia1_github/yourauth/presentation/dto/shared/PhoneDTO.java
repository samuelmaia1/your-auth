package com.samuelmaia1_github.yourauth.presentation.dto.shared;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PhoneDTO(
        @NotBlank
        @Pattern(regexp = "\\d{2}")
        String ddd,

        @NotBlank
        @Pattern(regexp = "\\d{8,9}")
        String number
) {
}
