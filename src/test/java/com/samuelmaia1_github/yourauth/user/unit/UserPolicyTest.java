package com.samuelmaia1_github.yourauth.user.unit;

import com.samuelmaia1_github.yourauth.domain.user.User;
import com.samuelmaia1_github.yourauth.domain.user.UserPolicy;
import com.samuelmaia1_github.yourauth.domain.user.UserRepository;
import com.samuelmaia1_github.yourauth.domain.user.exceptions.UserAlreadyExistsException;
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
public class UserPolicyTest {
    @Mock
    UserRepository repository;

    @InjectMocks
    UserPolicy policy;

    @Test
    void shouldAllowCreateUserWhenEmailAndCPFAreUnique() {
        User user = mock(User.class);

        when(user.getEmail()).thenReturn("email@email.com");
        CPF cpf = new CPF("12345678909");
        when(user.getCPF()).thenReturn(cpf);

        when(repository.findByEmail("email@email.com")).thenReturn(Optional.empty());
        when(repository.findByCPF(cpf)).thenReturn(Optional.empty());

        assertThatNoException().isThrownBy(() -> policy.ensureCanCreate(user));
    }

    @Test
    void shouldNotAllowCreateUserWhenEmailAlreadyExists() {
        User user = mock(User.class);

        when(user.getEmail()).thenReturn("email@email.com");

        when(repository.findByEmail("email@email.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> policy.ensureCanCreate(user))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("E-mail já cadastrado");
    }

    @Test
    void shouldNotAllowCreateUserWhenCPFAlreadyExists() {
        User user = mock(User.class);

        CPF cpf = new CPF("12345678909");
        when(user.getCPF()).thenReturn(cpf);

        when(repository.findByCPF(cpf)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> policy.ensureCanCreate(user))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("CPF já cadastrado");
    }
}
