package com.samuelmaia1_github.yourauth.presentation.dto.shared;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddressDTO(
        @NotBlank
        @Pattern(regexp = "\\d{8}|\\d{5}-\\d{3}")
        String cep,

        @NotBlank
        @Size(max = 255)
        String street,

        @NotBlank
        @Size(max = 120)
        String neighborhood,

        @NotBlank
        @Size(max = 120)
        String city,

        @NotBlank
        @Size(min = 2, max = 120)
        String state,

        @NotBlank
        @Size(max = 30)
        String number
) {
}
