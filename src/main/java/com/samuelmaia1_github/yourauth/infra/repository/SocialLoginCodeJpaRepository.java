package com.samuelmaia1_github.yourauth.infra.repository;

import com.samuelmaia1_github.yourauth.infra.repository.entity.SocialLoginCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface SocialLoginCodeJpaRepository extends JpaRepository<SocialLoginCodeEntity, String> {
    Optional<SocialLoginCodeEntity> findByHash(String hash);

    @Modifying
    @Query(value = """
            UPDATE social_login_codes
            SET consumed_at = :consumedAt
            WHERE hash = :hash
              AND consumed_at IS NULL
              AND expires_at > :consumedAt
            """, nativeQuery = true)
    int consumeValid(
            @Param("hash") String hash,
            @Param("consumedAt") Instant consumedAt
    );
}
