package com.samuelmaia1_github.yourauth.infra.repository.adapter;

import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMember;
import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMemberRepository;
import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMemberRole;
import com.samuelmaia1_github.yourauth.infra.mappers.ProjectMemberMapper;
import com.samuelmaia1_github.yourauth.infra.repository.ProjectMemberJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProjectMemberRepositoryAdapter implements ProjectMemberRepository {
    private final ProjectMemberJpaRepository repository;

    @Override
    public ProjectMember save(ProjectMember projectMember) {
        return ProjectMemberMapper.toDomain(repository.save(ProjectMemberMapper.toEntity(projectMember)));
    }

    @Override
    public Optional<ProjectMember> findByProjectIdAndAccountId(String projectId, String accountId) {
        return repository.findByProjectIdAndAccountId(projectId, accountId).map(ProjectMemberMapper::toDomain);
    }

    @Override
    public boolean existsByProjectIdAndAccountId(String projectId, String accountId) {
        return repository.existsByProjectIdAndAccountId(projectId, accountId);
    }

    @Override
    public boolean existsByProjectIdAndAccountIdAndRoleIn(
            String projectId,
            String accountId,
            Collection<ProjectMemberRole> roles
    ) {
        return repository.existsByProjectIdAndAccountIdAndRoleIn(projectId, accountId, roles);
    }

    @Override
    public void deleteAllByProjectId(String projectId) {
        repository.deleteAllByProjectId(projectId);
    }
}
