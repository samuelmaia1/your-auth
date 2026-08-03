package com.samuelmaia1_github.yourauth.infra.repository;

import com.samuelmaia1_github.yourauth.infra.repository.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenEntity, String> {
    Optional<RefreshTokenEntity> findByHash(String hash);

    List<RefreshTokenEntity> findAllByUserId(String userId);

    List<RefreshTokenEntity> findAllByFamilyId(String familyId);
}
