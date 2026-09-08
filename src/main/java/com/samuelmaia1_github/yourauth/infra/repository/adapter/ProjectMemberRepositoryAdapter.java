package com.samuelmaia1_github.yourauth.infra.repository.adapter;

import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMember;
import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMemberDetails;
import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMemberDetailsRepository;
import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMemberRepository;
import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMemberRole;
import com.samuelmaia1_github.yourauth.domain.shared.PageResult;
import com.samuelmaia1_github.yourauth.domain.shared.Pagination;
import com.samuelmaia1_github.yourauth.infra.mappers.ProjectMemberMapper;
import com.samuelmaia1_github.yourauth.infra.repository.ProjectMemberDetailsProjection;
import com.samuelmaia1_github.yourauth.infra.repository.ProjectMemberJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProjectMemberRepositoryAdapter implements ProjectMemberRepository, ProjectMemberDetailsRepository {
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
    public PageResult<ProjectMemberDetails> findAllByProjectId(String projectId, Pagination pagination) {
        Page<ProjectMemberDetails> page = repository.findAllDetailsByProjectId(
                projectId,
                pageRequest(pagination)
        ).map(this::toDetails);

        return toPageResult(page);
    }

    @Override
    public void deleteAllByProjectId(String projectId) {
        repository.deleteAllByProjectId(projectId);
    }

    private ProjectMemberDetails toDetails(ProjectMemberDetailsProjection projection) {
        return new ProjectMemberDetails(
                projection.name(),
                projection.lastName(),
                projection.role(),
                projection.joinedAt()
        );
    }

    private PageRequest pageRequest(Pagination pagination) {
        return PageRequest.of(
                pagination.page(),
                pagination.size()
        );
    }

    private PageResult<ProjectMemberDetails> toPageResult(Page<ProjectMemberDetails> page) {
        return new PageResult<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
