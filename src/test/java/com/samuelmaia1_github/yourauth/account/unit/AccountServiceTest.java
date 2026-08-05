package com.samuelmaia1_github.yourauth.account.unit;

import com.samuelmaia1_github.yourauth.domain.account.Account;
import com.samuelmaia1_github.yourauth.domain.account.AccountRepository;
import com.samuelmaia1_github.yourauth.domain.account.AccountService;
import com.samuelmaia1_github.yourauth.domain.account.AccountPolicy;
import com.samuelmaia1_github.yourauth.infra.interfaces.IPasswordEncoder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {
    @Mock
    AccountRepository repository;

    @Mock
    AccountPolicy policy;

    @Mock
    IPasswordEncoder encoder;

    @InjectMocks
    AccountService service;

    @Test
    void shouldCreateAccount() {
        Account account = Account.builder()
                .email("email@email.com")
                .password("raw-password")
                .build();

        when(encoder.encode("raw-password")).thenReturn("encoded-password");
        when(repository.save(account)).thenReturn(account);

        Account createdAccount = service.create(account);

        verify(policy).ensureCanCreate(account);
        verify(encoder).encode("raw-password");
        verify(repository).save(account);
        assertThat(createdAccount).isSameAs(account);
        assertThat(createdAccount.getPassword()).isEqualTo("encoded-password");
    }
}
