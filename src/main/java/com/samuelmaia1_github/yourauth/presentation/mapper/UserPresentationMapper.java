package com.samuelmaia1_github.yourauth.presentation.mapper;

import com.samuelmaia1_github.yourauth.domain.user.User;
import com.samuelmaia1_github.yourauth.domain.valueobjects.CPF;
import com.samuelmaia1_github.yourauth.infra.utils.Formatter;
import com.samuelmaia1_github.yourauth.presentation.dto.user.CreateUserDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.user.UserResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class UserPresentationMapper {

    public static User toDomain(CreateUserDTO dto) {
        return User
                .builder()
                .name(dto.name())
                .lastName(dto.lastName())
                .email(dto.email())
                .CPF(new CPF(Formatter.normalizeCpf(dto.CPF())))
                .address(AddressPresentationMapper.toDomain(dto.address()))
                .phone(PhonePresentationMapper.toDomain(dto.phone()))
                .password(dto.password())
                .build();
    }

    public static UserResponseDTO toResponseDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getLastName(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                AddressPresentationMapper.toDTO(user.getAddress()),
                PhonePresentationMapper.toDTO(user.getPhone()),
                user.getCPF().toString()
        );
    }
}
