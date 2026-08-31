package com.samuelmaia1_github.yourauth.infra.repository;

import com.samuelmaia1_github.yourauth.infra.repository.entity.AuthConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthConfigJpaRepository extends JpaRepository<AuthConfigEntity, String> {
    Optional<AuthConfigEntity> findByProjectId(String projectId);
}
