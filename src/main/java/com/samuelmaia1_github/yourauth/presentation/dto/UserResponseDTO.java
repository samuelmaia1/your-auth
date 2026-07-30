package com.samuelmaia1_github.yourauth.presentation.dto;

import com.samuelmaia1_github.yourauth.presentation.dto.shared.AddressDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.shared.PhoneDTO;

import java.time.LocalDateTime;

public record UserResponseDTO(
        String id,
        String name,
        String lastName,
        String email,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        AddressDTO address,
        PhoneDTO phone,
        String CPF
) {
}
