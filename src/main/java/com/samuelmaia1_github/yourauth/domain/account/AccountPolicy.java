package com.samuelmaia1_github.yourauth.domain.account;

import com.samuelmaia1_github.yourauth.domain.account.exceptions.AccountAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountPolicy {
    private final AccountRepository repository;

    public void ensureCanCreate(Account account) {
        if (repository.findByEmailIgnoreCase(account.getEmail()).isPresent())
            throw new AccountAlreadyExistsException("E-mail já cadastrado");

        if (account.getCPF() != null && repository.findByCPF(account.getCPF()).isPresent())
            throw new AccountAlreadyExistsException("CPF já cadastrado");
    }
}
