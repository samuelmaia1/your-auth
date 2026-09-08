package com.samuelmaia1_github.yourauth.infra.repository;

import com.samuelmaia1_github.yourauth.infra.repository.entity.ProjectMemberEntity;
import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMemberRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;

public interface ProjectMemberJpaRepository extends JpaRepository<ProjectMemberEntity, String> {
    Optional<ProjectMemberEntity> findByProjectIdAndAccountId(String projectId, String accountId);

    boolean existsByProjectIdAndAccountId(String projectId, String accountId);

    boolean existsByProjectIdAndAccountIdAndRoleIn(
            String projectId,
            String accountId,
            Collection<ProjectMemberRole> roles
    );

    @Query(
            value = """
                    select new com.samuelmaia1_github.yourauth.infra.repository.ProjectMemberDetailsProjection(
                        account.name,
                        account.lastName,
                        projectMember.role,
                        projectMember.joinedAt
                    )
                    from ProjectMemberEntity projectMember
                    join AccountEntity account
                        on account.id = projectMember.accountId
                    where projectMember.projectId = :projectId
                    order by projectMember.joinedAt desc
                    """,
            countQuery = """
                    select count(projectMember)
                    from ProjectMemberEntity projectMember
                    where projectMember.projectId = :projectId
                    """
    )
    Page<ProjectMemberDetailsProjection> findAllDetailsByProjectId(
            @Param("projectId") String projectId,
            Pageable pageable
    );

    void deleteAllByProjectId(String projectId);
}
