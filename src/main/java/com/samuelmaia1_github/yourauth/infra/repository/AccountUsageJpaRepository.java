package com.samuelmaia1_github.yourauth.infra.repository;

import com.samuelmaia1_github.yourauth.infra.repository.entity.ProjectEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface AccountUsageJpaRepository extends Repository<ProjectEntity, String> {
    @Query("""
            select new com.samuelmaia1_github.yourauth.infra.repository.AccountUsageProjection(
                count(distinct project.id),
                count(distinct projectUser.id),
                count(distinct activeSession.id)
            )
            from ProjectEntity project
            left join UserEntity projectUser on projectUser.projectId = project.id
            left join UserSessionEntity activeSession on activeSession.projectId = project.id
                and activeSession.revokedAt is null
            where project.ownerAccountId = :accountId
            """)
    AccountUsageProjection findByOwnerAccountId(@Param("accountId") String accountId);
}
