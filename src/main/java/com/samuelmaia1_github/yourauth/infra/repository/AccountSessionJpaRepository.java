package com.samuelmaia1_github.yourauth.infra.repository;

import com.samuelmaia1_github.yourauth.infra.repository.entity.AccountSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AccountSessionJpaRepository extends JpaRepository<AccountSessionEntity, String> {
    List<AccountSessionEntity> findAllByAccountId(String accountId);

    List<AccountSessionEntity> findAllByAccountIdAndRevokedAtIsNull(String accountId);

    @Modifying
    @Query(value = """
        UPDATE account_sessions
        SET revoked_at = CURRENT_TIMESTAMP
        WHERE id = :id
          AND revoked_at IS NULL
        """, nativeQuery = true)
    int revokeById(@Param("id") String id);

    @Modifying
    @Query(value = """
        UPDATE account_sessions
        SET revoked_at = CURRENT_TIMESTAMP
        WHERE account_id = :accountId
          AND revoked_at IS NULL
        """, nativeQuery = true)
    int revokeAllByAccountId(@Param("accountId") String accountId);

    long countByAccountIdAndRevokedAtIsNull(String accountId);
}
