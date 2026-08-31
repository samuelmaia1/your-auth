package com.samuelmaia1_github.yourauth.infra.repository;

import com.samuelmaia1_github.yourauth.infra.repository.entity.UserRefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRefreshTokenJpaRepository extends JpaRepository<UserRefreshTokenEntity, String> {
    Optional<UserRefreshTokenEntity> findByHash(String hash);

    List<UserRefreshTokenEntity> findAllByProjectIdAndUserId(String projectId, String userId);

    List<UserRefreshTokenEntity> findAllBySessionId(String sessionId);

    @Modifying
    @Query(value = """
        UPDATE user_refresh_tokens
        SET revoked_at = CURRENT_TIMESTAMP
        WHERE session_id = :sessionId
          AND revoked_at IS NULL
        """, nativeQuery = true)
    int revokeSession(@Param("sessionId") String sessionId);
}
