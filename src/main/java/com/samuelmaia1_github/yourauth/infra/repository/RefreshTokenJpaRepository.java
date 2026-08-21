package com.samuelmaia1_github.yourauth.infra.repository;

import com.samuelmaia1_github.yourauth.infra.repository.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenEntity, String> {
    Optional<RefreshTokenEntity> findByHash(String hash);

    List<RefreshTokenEntity> findAllByAccountId(String accountId);

    List<RefreshTokenEntity> findAllByFamilyId(String familyId);

    @Modifying
    @Query(value = """
        UPDATE refresh_tokens
        SET revoked_at = CURRENT_TIMESTAMP
        WHERE family_id = :familyId
          AND revoked_at IS NULL
        """, nativeQuery = true)
    int revokeFamily(@Param("familyId") String familyId);
}
