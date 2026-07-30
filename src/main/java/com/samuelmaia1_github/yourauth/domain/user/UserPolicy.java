package com.samuelmaia1_github.yourauth.domain.user;

import com.samuelmaia1_github.yourauth.domain.user.exceptions.UserAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserPolicy {
    private final UserRepository repository;

    public void ensureCanCreate(User user) {
        if (repository.findByEmail(user.getEmail()).isPresent())
            throw new UserAlreadyExistsException("E-mail já cadastrado");

        if (repository.findByCPF(user.getCPF()).isPresent())
            throw new UserAlreadyExistsException("CPF já cadastrado");
    }
}
