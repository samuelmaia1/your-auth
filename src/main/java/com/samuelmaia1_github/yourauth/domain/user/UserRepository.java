package com.samuelmaia1_github.yourauth.domain.user;

import com.samuelmaia1_github.yourauth.domain.valueobjects.CPF;

import java.util.Optional;

public interface UserRepository {
    User save(User user);

    Optional<User> findById(String id);

    Optional<User> findByEmail(String email);

    Optional<User> findByCPF(CPF cpf);

    Optional<User> findByEmailIgnoreCaseOrCPF(String email, CPF cpf);

    void deleteById(String id);
}
