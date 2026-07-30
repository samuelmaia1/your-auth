package com.samuelmaia1_github.yourauth.user.unit;

import com.samuelmaia1_github.yourauth.domain.user.User;
import com.samuelmaia1_github.yourauth.domain.user.UserRepository;
import com.samuelmaia1_github.yourauth.domain.user.UserService;
import com.samuelmaia1_github.yourauth.domain.user.UserPolicy;
import com.samuelmaia1_github.yourauth.infra.interfaces.IPasswordEncoder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    UserRepository repository;

    @Mock
    UserPolicy policy;

    @Mock
    IPasswordEncoder encoder;

    @InjectMocks
    UserService service;

    @Test
    void shouldCreateUser() {
        User user = User.builder()
                .email("email@email.com")
                .password("raw-password")
                .build();

        when(encoder.encode("raw-password")).thenReturn("encoded-password");
        when(repository.save(user)).thenReturn(user);

        User createdUser = service.create(user);

        verify(policy).ensureCanCreate(user);
        verify(encoder).encode("raw-password");
        verify(repository).save(user);
        assertThat(createdUser).isSameAs(user);
        assertThat(createdUser.getPassword()).isEqualTo("encoded-password");
    }
}
