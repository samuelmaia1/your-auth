package com.samuelmaia1_github.yourauth.presentation.dto.project;

import com.samuelmaia1_github.yourauth.domain.project.ProjectEnvironment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateProjectDTO(
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 100)
        String name,

        @Size(max = 255)
        String description,

        @NotNull(message = "O ambiente é obrigatório")
        ProjectEnvironment environment,

        @NotBlank(message = "A audiência do token é obrigatória")
        @Size(max = 255)
        String tokenAudience
) {
}
