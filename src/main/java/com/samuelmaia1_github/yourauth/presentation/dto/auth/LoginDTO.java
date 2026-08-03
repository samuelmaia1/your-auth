package com.samuelmaia1_github.yourauth.presentation.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.br.CPF;

public record LoginDTO(
        @NotBlank
        String password,
        @Email(message = "O e-mail informado é inválido.")
        String email,
        @CPF
        String cpf
) {
}
