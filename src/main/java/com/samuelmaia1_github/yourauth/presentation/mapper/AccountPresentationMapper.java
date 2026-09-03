package com.samuelmaia1_github.yourauth.presentation.mapper;

import com.samuelmaia1_github.yourauth.domain.account.Account;
import com.samuelmaia1_github.yourauth.domain.valueobjects.CPF;
import com.samuelmaia1_github.yourauth.infra.utils.Formatter;
import com.samuelmaia1_github.yourauth.presentation.dto.account.AccountBasicResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.account.CreateAccountDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.account.AccountResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class AccountPresentationMapper {

    public static Account toDomain(CreateAccountDTO dto) {
        return Account
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

    public static AccountResponseDTO toResponseDTO(Account account) {
        return new AccountResponseDTO(
                account.getId(),
                account.getName(),
                account.getLastName(),
                account.getEmail(),
                account.getCreatedAt(),
                account.getUpdatedAt(),
                AddressPresentationMapper.toDTO(account.getAddress()),
                PhonePresentationMapper.toDTO(account.getPhone()),
                account.getCPF().toString()
        );
    }

    public static AccountBasicResponseDTO toBasicResponseDTO(Account account) {
        return new AccountBasicResponseDTO(
                account.getId(),
                account.getName(),
                account.getLastName(),
                account.getEmail()
        );
    }
}
