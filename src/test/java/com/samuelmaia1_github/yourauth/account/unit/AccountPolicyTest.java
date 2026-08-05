package com.samuelmaia1_github.yourauth.account.unit;

import com.samuelmaia1_github.yourauth.domain.account.Account;
import com.samuelmaia1_github.yourauth.domain.account.AccountPolicy;
import com.samuelmaia1_github.yourauth.domain.account.AccountRepository;
import com.samuelmaia1_github.yourauth.domain.account.exceptions.AccountAlreadyExistsException;
import com.samuelmaia1_github.yourauth.domain.valueobjects.CPF;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountPolicyTest {
    @Mock
    AccountRepository repository;

    @InjectMocks
    AccountPolicy policy;

    @Test
    void shouldAllowCreateAccountWhenEmailAndCPFAreUnique() {
        Account account = mock(Account.class);

        when(account.getEmail()).thenReturn("email@email.com");
        CPF cpf = new CPF("12345678909");
        when(account.getCPF()).thenReturn(cpf);

        when(repository.findByEmail("email@email.com")).thenReturn(Optional.empty());
        when(repository.findByCPF(cpf)).thenReturn(Optional.empty());

        assertThatNoException().isThrownBy(() -> policy.ensureCanCreate(account));
    }

    @Test
    void shouldNotAllowCreateAccountWhenEmailAlreadyExists() {
        Account account = mock(Account.class);

        when(account.getEmail()).thenReturn("email@email.com");

        when(repository.findByEmail("email@email.com")).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> policy.ensureCanCreate(account))
                .isInstanceOf(AccountAlreadyExistsException.class)
                .hasMessage("E-mail já cadastrado");
    }

    @Test
    void shouldNotAllowCreateAccountWhenCPFAlreadyExists() {
        Account account = mock(Account.class);

        CPF cpf = new CPF("12345678909");
        when(account.getCPF()).thenReturn(cpf);

        when(repository.findByCPF(cpf)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> policy.ensureCanCreate(account))
                .isInstanceOf(AccountAlreadyExistsException.class)
                .hasMessage("CPF já cadastrado");
    }
}
