package com.samuelmaia1_github.yourauth.infra.repository.adapter;

import com.samuelmaia1_github.yourauth.domain.account.Account;
import com.samuelmaia1_github.yourauth.domain.account.AccountRepository;
import com.samuelmaia1_github.yourauth.domain.valueobjects.CPF;
import com.samuelmaia1_github.yourauth.infra.mappers.AccountMapper;
import com.samuelmaia1_github.yourauth.infra.repository.AccountJpaRepository;
import com.samuelmaia1_github.yourauth.infra.repository.entity.AccountEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AccountRepositoryAdapter implements AccountRepository {
    private final AccountJpaRepository repository;

    @Override
    public Account save(Account account) {
        return AccountMapper.toDomain(repository.save(AccountMapper.toEntity(account)));
    }

    @Override
    public Optional<Account> findById(String id) {
        return repository.findById(id).map(AccountMapper::toDomain);
    }

    @Override
    public Optional<Account> findByEmail(String email) {
        return repository.findByEmail(email).map(AccountMapper::toDomain);
    }

    @Override
    public Optional<Account> findByCPF(CPF cpf) {
        return repository.findByCPF(cpf).map(AccountMapper::toDomain);
    }

    @Override
    public Optional<Account> findByEmailIgnoreCaseOrCPF(String email, CPF cpf) {
        return repository.findByEmailIgnoreCaseOrCPF(email, cpf).map(AccountMapper::toDomain);
    }

    @Override
    public void deleteById(String id) {

    }
}
