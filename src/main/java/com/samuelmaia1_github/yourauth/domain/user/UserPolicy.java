package com.samuelmaia1_github.yourauth.domain.user;

import com.samuelmaia1_github.yourauth.domain.user.exceptions.UserAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserPolicy {
    private final UserRepository repository;

    public void ensureCanCreate(User user) {
        if (repository.existsByProjectIdAndEmailIgnoreCase(user.getProjectId(), user.getEmail())) {
            throw new UserAlreadyExistsException("Usuário já cadastrado para este projeto.");
        }
    }

    public void ensureCanUpdate(User user) {
        if (repository.existsByProjectIdAndEmailIgnoreCaseAndIdNot(
                user.getProjectId(),
                user.getEmail(),
                user.getId()
        )) {
            throw new UserAlreadyExistsException("Usuário já cadastrado para este projeto.");
        }
    }
}
