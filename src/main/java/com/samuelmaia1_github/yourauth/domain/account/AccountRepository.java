package com.samuelmaia1_github.yourauth.domain.account;

import com.samuelmaia1_github.yourauth.domain.valueobjects.CPF;

import java.util.Optional;

public interface AccountRepository {
    Account save(Account account);

    Optional<Account> findById(String id);

    Optional<Account> findByEmail(String email);

    Optional<Account> findByCPF(CPF cpf);

    Optional<Account> findByEmailIgnoreCaseOrCPF(String email, CPF cpf);

    void deleteById(String id);
}
