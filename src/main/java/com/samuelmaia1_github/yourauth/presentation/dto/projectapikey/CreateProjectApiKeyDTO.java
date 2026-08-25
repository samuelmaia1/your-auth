package com.samuelmaia1_github.yourauth.presentation.dto.projectapikey;

import com.samuelmaia1_github.yourauth.domain.projectapikey.ProjectApiKeyScope;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record CreateProjectApiKeyDTO(
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 100)
        String name,

        @NotEmpty(message = "Informe ao menos uma permissão")
        Set<@NotNull(message = "A permissão não pode ser nula") ProjectApiKeyScope> scopes,

        @Positive(message = "A expiração deve ser maior que zero")
        @Max(value = 87600, message = "A expiração deve ser de no máximo 87600 horas")
        Long expiresInHours
) {
}
