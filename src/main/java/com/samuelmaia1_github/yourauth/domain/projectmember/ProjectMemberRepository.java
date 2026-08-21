package com.samuelmaia1_github.yourauth.domain.projectmember;

import java.util.Collection;
import java.util.Optional;

public interface ProjectMemberRepository {
    ProjectMember save(ProjectMember projectMember);

    Optional<ProjectMember> findByProjectIdAndAccountId(String projectId, String accountId);

    boolean existsByProjectIdAndAccountId(String projectId, String accountId);

    boolean existsByProjectIdAndAccountIdAndRoleIn(
            String projectId,
            String accountId,
            Collection<ProjectMemberRole> roles
    );

    void deleteAllByProjectId(String projectId);
}
