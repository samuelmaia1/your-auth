package com.samuelmaia1_github.yourauth.presentation.dto.user;

import com.samuelmaia1_github.yourauth.presentation.dto.shared.AddressDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.shared.PhoneDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;

public record CreateUserDTO(
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 255)
        String name,

        @NotBlank(message = "O sobrenome é obrigatório")
        @Size(max = 255)
        String lastName,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "O e-mail informado é inválido.")
        @Size(max = 320)
        String email,

        @NotBlank(message = "A senha é obrigatória")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
                message = "A senha deve ter no mínimo 8 caracteres, uma letra maiúscula, uma letra minúscula e um número"
        )
        @Size(max = 255)
        String password,

        @NotBlank
        @CPF
        String CPF,

        @NotNull
        @Valid
        AddressDTO address,

        @NotNull
        @Valid
        PhoneDTO phone
) {
}
