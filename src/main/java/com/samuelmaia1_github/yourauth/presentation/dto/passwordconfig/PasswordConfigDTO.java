package com.samuelmaia1_github.yourauth.presentation.dto.passwordconfig;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record PasswordConfigDTO(
    @Min(value = 1, message = "O tamanho mínimo deve ser pelo menos 1")
    @Max(value = 120, message = "O tamanho mínimo deve ser no máximo 120")
    Integer minSize,

    @Min(value = 1, message = "O tamanho máximo deve ser pelo menos 1")
    @Max(value = 120, message = "O tamanho máximo deve ser no máximo 120")
    Integer maxSize,

    Boolean numberRequired,

    Boolean uppercaseRequired,

    Boolean lowercaseRequired,

    Boolean specialCharRequired
) {
    @AssertTrue(message = "O tamanho mínimo não pode ser maior que o tamanho máximo.")
    public boolean isValidRange() {
        if (minSize == null || maxSize == null) return true;
        return minSize <= maxSize;
    }
}
