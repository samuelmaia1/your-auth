package com.samuelmaia1_github.yourauth.infra.repository;

import com.samuelmaia1_github.yourauth.infra.repository.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserEntity, String> {
    Optional<UserEntity> findByProjectIdAndId(String projectId, String id);

    Optional<UserEntity> findByProjectIdAndEmailIgnoreCase(String projectId, String email);

    boolean existsByProjectIdAndEmailIgnoreCase(String projectId, String email);

    boolean existsByProjectIdAndEmailIgnoreCaseAndIdNot(String projectId, String email, String id);

    Page<UserEntity> findAllByProjectId(String projectId, Pageable pageable);
}
