package com.samuelmaia1_github.yourauth.infra.repository;

import com.samuelmaia1_github.yourauth.infra.repository.entity.UserSessionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
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
                      and (:activeOnly = false or userSession.revokedAt is null)
                      and (:inactiveOnly = false or userSession.revokedAt is not null)
                      and (:lastUsedAtFrom is null or userSession.lastUsedAt >= :lastUsedAtFrom)
                      and (:lastUsedAtTo is null or userSession.lastUsedAt <= :lastUsedAtTo)
                      and (:userEmail is null or lower(sessionUser.email) like lower(concat('%', :userEmail, '%')))
                    """,
            countQuery = """
                    select count(userSession)
                    from UserSessionEntity userSession
                    join UserEntity sessionUser
                        on sessionUser.projectId = userSession.projectId
                        and sessionUser.id = userSession.userId
                    where userSession.projectId = :projectId
                      and (:activeOnly = false or userSession.revokedAt is null)
                      and (:inactiveOnly = false or userSession.revokedAt is not null)
                      and (:lastUsedAtFrom is null or userSession.lastUsedAt >= :lastUsedAtFrom)
                      and (:lastUsedAtTo is null or userSession.lastUsedAt <= :lastUsedAtTo)
                      and (:userEmail is null or lower(sessionUser.email) like lower(concat('%', :userEmail, '%')))
                    """
    )
    Page<UserSessionDetailsProjection> findAllDetailsByProjectId(
            @Param("projectId") String projectId,
            @Param("activeOnly") boolean activeOnly,
            @Param("inactiveOnly") boolean inactiveOnly,
            @Param("lastUsedAtFrom") Instant lastUsedAtFrom,
            @Param("lastUsedAtTo") Instant lastUsedAtTo,
            @Param("userEmail") String userEmail,
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
