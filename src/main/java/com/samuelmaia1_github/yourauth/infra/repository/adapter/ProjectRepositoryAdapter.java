package com.samuelmaia1_github.yourauth.infra.repository.adapter;

import com.samuelmaia1_github.yourauth.domain.project.Project;
import com.samuelmaia1_github.yourauth.domain.project.ProjectRepository;
import com.samuelmaia1_github.yourauth.domain.shared.PageResult;
import com.samuelmaia1_github.yourauth.domain.shared.Pagination;
import com.samuelmaia1_github.yourauth.infra.mappers.ProjectMapper;
import com.samuelmaia1_github.yourauth.infra.repository.ProjectJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProjectRepositoryAdapter implements ProjectRepository {
    private final ProjectJpaRepository repository;

    @Override
    public Project save(Project project) {
        return ProjectMapper.toDomain(repository.save(ProjectMapper.toEntity(project)));
    }

    @Override
    public Optional<Project> findById(String id) {
        return repository.findById(id).map(ProjectMapper::toDomain);
    }

    @Override
    public boolean existsByOwnerAccountIdAndName(String ownerAccountId, String name) {
        return repository.existsByOwnerAccountIdAndName(ownerAccountId, name);
    }

    @Override
    public boolean existsByOwnerAccountIdAndNameAndIdNot(String ownerAccountId, String name, String id) {
        return repository.existsByOwnerAccountIdAndNameAndIdNot(ownerAccountId, name, id);
    }

    @Override
    public PageResult<Project> findAllByOwnerAccountId(String id, Pagination pagination) {
        Page<Project> page = repository.findAllByOwnerAccountId(
                id,
                pageRequest(pagination)
        ).map(ProjectMapper::toDomain);

        return toPageResult(page);
    }

    @Override
    public PageResult<Project> findAllByMemberAccountId(String accountId, Pagination pagination) {
        Page<Project> page = repository.findAllByMemberAccountId(
                accountId,
                pageRequest(pagination)
        ).map(ProjectMapper::toDomain);

        return toPageResult(page);
    }

    @Override
    public void deleteById(String id) {
        repository.deleteById(id);
    }

    private PageRequest pageRequest(Pagination pagination) {
        return PageRequest.of(
                pagination.page(),
                pagination.size(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
    }

    private PageResult<Project> toPageResult(Page<Project> page) {
        return new PageResult<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
