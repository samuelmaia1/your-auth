package com.samuelmaia1_github.yourauth.infra.repository;

import com.samuelmaia1_github.yourauth.infra.repository.entity.ProjectEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectJpaRepository extends JpaRepository<ProjectEntity, String> {
    Page<ProjectEntity> findAllByOwnerAccountId(String ownerAccountId, Pageable pageable);

    boolean existsByOwnerAccountIdAndName(String ownerAccountId, String name);

    boolean existsByOwnerAccountIdAndNameAndIdNot(String ownerAccountId, String name, String id);

    @Query("""
            select project from ProjectEntity project
            where exists (
                select 1 from ProjectMemberEntity projectMember
                where projectMember.projectId = project.id
                and projectMember.accountId = :accountId
            )
            """)
    Page<ProjectEntity> findAllByMemberAccountId(@Param("accountId") String accountId, Pageable pageable);
}
