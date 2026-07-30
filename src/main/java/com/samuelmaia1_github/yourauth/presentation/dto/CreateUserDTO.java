package com.samuelmaia1_github.yourauth.presentation.dto;

import com.samuelmaia1_github.yourauth.presentation.dto.shared.AddressDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.shared.PhoneDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;

public record CreateUserDTO(
        @NotBlank
        @Size(max = 255)
        String name,

        @NotBlank
        @Size(max = 255)
        String lastName,

        @NotBlank
        @Email
        @Size(max = 320)
        String email,

        @NotBlank
        @Size(min = 8, max = 255)
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
