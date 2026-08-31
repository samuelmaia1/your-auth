package com.samuelmaia1_github.yourauth.infra.repository.adapter;

import com.samuelmaia1_github.yourauth.domain.projectapikey.ProjectApiKey;
import com.samuelmaia1_github.yourauth.domain.projectapikey.ProjectApiKeyRepository;
import com.samuelmaia1_github.yourauth.domain.shared.PageResult;
import com.samuelmaia1_github.yourauth.domain.shared.Pagination;
import com.samuelmaia1_github.yourauth.infra.mappers.ProjectApiKeyMapper;
import com.samuelmaia1_github.yourauth.infra.repository.ProjectApiKeyJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProjectApiKeyRepositoryAdapter implements ProjectApiKeyRepository {
    private final ProjectApiKeyJpaRepository repository;

    @Override
    public ProjectApiKey save(ProjectApiKey apiKey) {
        return ProjectApiKeyMapper.toDomain(repository.save(ProjectApiKeyMapper.toEntity(apiKey)));
    }

    @Override
    public Optional<ProjectApiKey> findById(String id) {
        return repository.findById(id).map(ProjectApiKeyMapper::toDomain);
    }

    @Override
    public Optional<ProjectApiKey> findByProjectIdAndId(String projectId, String id) {
        return repository.findByProjectIdAndId(projectId, id).map(ProjectApiKeyMapper::toDomain);
    }

    @Override
    public Optional<ProjectApiKey> findByKeyId(String keyId) {
        return repository.findByKeyId(keyId).map(ProjectApiKeyMapper::toDomain);
    }

    @Override
    public Optional<ProjectApiKey> findByPrefix(String prefix) {
        return repository.findByPrefix(prefix).map(ProjectApiKeyMapper::toDomain);
    }

    @Override
    public PageResult<ProjectApiKey> findAllByProjectId(String projectId, Pagination pagination) {
        Page<ProjectApiKey> page = repository.findAllByProjectId(
                projectId,
                pageRequest(pagination)
        ).map(ProjectApiKeyMapper::toDomain);

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

    private PageResult<ProjectApiKey> toPageResult(Page<ProjectApiKey> page) {
        return new PageResult<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
