package com.samuelmaia1_github.yourauth.infra.mappers;

import com.samuelmaia1_github.yourauth.domain.account.Account;
import com.samuelmaia1_github.yourauth.infra.repository.entity.AccountEntity;

public class AccountMapper {
    private AccountMapper() {
    }

    public static Account toDomain(AccountEntity entity) {
        if (entity == null) {
            return null;
        }

        return Account.builder()
                .id(entity.getId())
                .name(entity.getName())
                .lastName(entity.getLastName())
                .email(entity.getEmail())
                .password(entity.getPassword())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .address(entity.getAddress())
                .phone(entity.getPhone())
                .CPF(entity.getCPF())
                .build();
    }

    public static AccountEntity toEntity(Account account) {
        if (account == null) {
            return null;
        }

        return AccountEntity.builder()
                .id(account.getId())
                .name(account.getName())
                .lastName(account.getLastName())
                .email(account.getEmail())
                .password(account.getPassword())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .address(account.getAddress())
                .phone(account.getPhone())
                .CPF(account.getCPF())
                .build();
    }
}
