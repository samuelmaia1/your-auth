package com.samuelmaia1_github.yourauth.infra.repository;

import com.samuelmaia1_github.yourauth.domain.valueobjects.CPF;
import com.samuelmaia1_github.yourauth.infra.repository.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountJpaRepository extends JpaRepository<AccountEntity, String> {
    Optional<AccountEntity> findByEmail(String email);
    Optional<AccountEntity> findByCPF(CPF cpf);
    Optional<AccountEntity> findByEmailIgnoreCaseOrCPF(String email, CPF cpf);
}
