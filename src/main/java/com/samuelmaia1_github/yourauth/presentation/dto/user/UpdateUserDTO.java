package com.samuelmaia1_github.yourauth.presentation.dto.user;

import com.samuelmaia1_github.yourauth.presentation.dto.shared.PhoneDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserDTO(
        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "O e-mail informado é inválido.")
        @Size(max = 320)
        String email,

        @Size(max = 255)
        String password,

        @Valid
        PhoneDTO phone
) {
}
