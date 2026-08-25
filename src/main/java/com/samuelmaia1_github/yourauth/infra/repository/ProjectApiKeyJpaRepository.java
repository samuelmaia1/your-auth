package com.samuelmaia1_github.yourauth.infra.repository;

import com.samuelmaia1_github.yourauth.infra.repository.entity.ProjectApiKeyEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectApiKeyJpaRepository extends JpaRepository<ProjectApiKeyEntity, String> {
    Optional<ProjectApiKeyEntity> findByProjectIdAndId(String projectId, String id);

    Optional<ProjectApiKeyEntity> findByKeyId(String keyId);

    Optional<ProjectApiKeyEntity> findByPrefix(String prefix);

    Page<ProjectApiKeyEntity> findAllByProjectId(String projectId, Pageable pageable);
}
