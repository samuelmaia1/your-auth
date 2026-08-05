package com.samuelmaia1_github.yourauth.account.unit;

import com.samuelmaia1_github.yourauth.domain.account.Account;
import com.samuelmaia1_github.yourauth.domain.account.AccountPolicy;
import com.samuelmaia1_github.yourauth.domain.account.AccountRepository;
import com.samuelmaia1_github.yourauth.domain.account.exceptions.AccountAlreadyExistsException;
import com.samuelmaia1_github.yourauth.domain.valueobjects.CPF;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class AccountPolicyTest {
    @Test
    void shouldAllowCreateAccountWhenEmailAndCPFAreUnique() {
        CPF cpf = new CPF("12345678909");
        Account account = account(cpf);
        AccountPolicy policy = new AccountPolicy(new StubAccountRepository(Optional.empty(), Optional.empty()));

        assertThatNoException().isThrownBy(() -> policy.ensureCanCreate(account));
    }

    @Test
    void shouldNotAllowCreateAccountWhenEmailAlreadyExists() {
        Account account = account(null);
        AccountPolicy policy = new AccountPolicy(new StubAccountRepository(Optional.of(account), Optional.empty()));

        assertThatThrownBy(() -> policy.ensureCanCreate(account))
                .isInstanceOf(AccountAlreadyExistsException.class)
                .hasMessage("E-mail já cadastrado");
    }

    @Test
    void shouldNotAllowCreateAccountWhenCPFAlreadyExists() {
        CPF cpf = new CPF("12345678909");
        Account account = account(cpf);
        AccountPolicy policy = new AccountPolicy(new StubAccountRepository(Optional.empty(), Optional.of(account)));

        assertThatThrownBy(() -> policy.ensureCanCreate(account))
                .isInstanceOf(AccountAlreadyExistsException.class)
                .hasMessage("CPF já cadastrado");
    }

    private Account account(CPF cpf) {
        return Account.builder()
                .email("email@email.com")
                .CPF(cpf)
                .build();
    }

    private static class StubAccountRepository implements AccountRepository {
        private final Optional<Account> accountByEmail;
        private final Optional<Account> accountByCPF;

        private StubAccountRepository(Optional<Account> accountByEmail, Optional<Account> accountByCPF) {
            this.accountByEmail = accountByEmail;
            this.accountByCPF = accountByCPF;
        }

        @Override
        public Account save(Account account) {
            return account;
        }

        @Override
        public Optional<Account> findById(String id) {
            return Optional.empty();
        }

        @Override
        public Optional<Account> findByEmail(String email) {
            return accountByEmail;
        }

        @Override
        public Optional<Account> findByCPF(CPF cpf) {
            return accountByCPF;
        }

        @Override
        public Optional<Account> findByEmailIgnoreCaseOrCPF(String email, CPF cpf) {
            return Optional.empty();
        }

        @Override
        public void deleteById(String id) {
        }
    }
}
