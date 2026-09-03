package com.samuelmaia1_github.yourauth.infra.repository;

import com.samuelmaia1_github.yourauth.infra.repository.entity.UserSessionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserSessionJpaRepository extends JpaRepository<UserSessionEntity, String> {
    @Query(
            value = """
                    select userSession as session, sessionUser as sessionUser
                    from UserSessionEntity userSession
                    join UserEntity sessionUser
                        on sessionUser.projectId = userSession.projectId
                        and sessionUser.id = userSession.userId
                    where userSession.projectId = :projectId
                    """,
            countQuery = """
                    select count(userSession)
                    from UserSessionEntity userSession
                    where userSession.projectId = :projectId
                    """
    )
    Page<UserSessionDetailsProjection> findAllDetailsByProjectId(
            @Param("projectId") String projectId,
            Pageable pageable
    );

    List<UserSessionEntity> findAllByProjectIdAndUserId(String projectId, String userId);

    List<UserSessionEntity> findAllByProjectIdAndUserIdAndRevokedAtIsNull(String projectId, String userId);

    @Modifying
    @Query(value = """
        UPDATE user_sessions
        SET revoked_at = CURRENT_TIMESTAMP
        WHERE id = :id
          AND revoked_at IS NULL
        """, nativeQuery = true)
    int revokeById(@Param("id") String id);

    long countByProjectIdAndUserIdAndRevokedAtIsNull(String projectId, String userId);
}
