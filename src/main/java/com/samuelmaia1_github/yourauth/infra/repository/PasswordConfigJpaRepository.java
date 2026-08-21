package com.samuelmaia1_github.yourauth.infra.repository;

import com.samuelmaia1_github.yourauth.infra.repository.entity.PasswordConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordConfigJpaRepository extends JpaRepository<PasswordConfigEntity, String> {
    Optional<PasswordConfigEntity> findByProjectId(String projectId);
}
