package com.samuelmaia1_github.yourauth.infra.repository;

import com.samuelmaia1_github.yourauth.infra.repository.entity.ProjectEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AccountSummaryJpaRepository extends Repository<ProjectEntity, String> {
    @Query("""
            select new com.samuelmaia1_github.yourauth.infra.repository.AccountProjectSummaryProjection(
                project.id,
                project.name,
                project.description,
                project.ownerAccountId,
                project.status,
                project.environment,
                project.tokenAudience,
                project.createdAt,
                project.updatedAt,
                projectMember.role,
                count(distinct projectUser.id),
                count(distinct activeSession.id)
            )
            from ProjectEntity project
            join ProjectMemberEntity projectMember on projectMember.projectId = project.id
            left join UserEntity projectUser on projectUser.projectId = project.id
            left join UserSessionEntity activeSession on activeSession.projectId = project.id
                and activeSession.revokedAt is null
            where projectMember.accountId = :accountId
            group by project.id,
                project.name,
                project.description,
                project.ownerAccountId,
                project.status,
                project.environment,
                project.tokenAudience,
                project.createdAt,
                project.updatedAt,
                projectMember.role
            order by project.createdAt desc
            """)
    List<AccountProjectSummaryProjection> findProjectSummariesByAccountId(@Param("accountId") String accountId);
}
