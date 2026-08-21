package com.samuelmaia1_github.yourauth.infra.repository;

import com.samuelmaia1_github.yourauth.infra.repository.entity.ProjectMemberEntity;
import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMemberRole;
import org.springframework.data.jpa.repository.JpaRepository;

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

    void deleteAllByProjectId(String projectId);
}
