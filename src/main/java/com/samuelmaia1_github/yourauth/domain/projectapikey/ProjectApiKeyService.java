package com.samuelmaia1_github.yourauth.domain.projectapikey;

import com.samuelmaia1_github.yourauth.domain.account.Account;
import com.samuelmaia1_github.yourauth.domain.account.AccountRepository;
import com.samuelmaia1_github.yourauth.domain.account.exceptions.AccountNotFoundException;
import com.samuelmaia1_github.yourauth.domain.project.ProjectRepository;
import com.samuelmaia1_github.yourauth.domain.project.exceptions.ProjectAccessDeniedException;
import com.samuelmaia1_github.yourauth.domain.project.exceptions.ProjectNotFoundException;
import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMemberRepository;
import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMemberRole;
import com.samuelmaia1_github.yourauth.domain.projectapikey.exceptions.InvalidProjectApiKeyException;
import com.samuelmaia1_github.yourauth.domain.projectapikey.exceptions.ProjectApiKeyAlreadyRevokedException;
import com.samuelmaia1_github.yourauth.domain.projectapikey.exceptions.ProjectApiKeyNotFoundException;
import com.samuelmaia1_github.yourauth.domain.shared.PageResult;
import com.samuelmaia1_github.yourauth.domain.shared.Pagination;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.samuelmaia1_github.yourauth.infra.cache.names.ProjectApiKeyCacheNames.*;

@Service
@RequiredArgsConstructor
public class ProjectApiKeyService {
    private static final List<ProjectMemberRole> PROJECT_API_KEY_MANAGEMENT_ROLES = List.of(
            ProjectMemberRole.OWNER,
            ProjectMemberRole.ADMIN
    );

    private final ProjectApiKeyRepository repository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final AccountRepository accountRepository;
    private final ProjectApiKeyGenerator generator;
    private final ProjectApiKeyHasher hasher;

    @Transactional
    @CacheEvict(
            cacheNames = PROJECT_API_KEYS_BY_PROJECT_ID,
            allEntries = true
    )
    public CreatedProjectApiKey create(ProjectApiKey requestedApiKey, String accountId) {
        ensureProjectExists(requestedApiKey.getProjectId());
        ensureCanManage(requestedApiKey.getProjectId(), accountId);
        ensureCanCreate(requestedApiKey);

        GeneratedProjectApiKey generatedKey = generator.generate();
        ProjectApiKey apiKey = ProjectApiKey.builder()
                .projectId(requestedApiKey.getProjectId())
                .name(requestedApiKey.getName())
                .keyId(generatedKey.keyId())
                .prefix(generatedKey.prefix())
                .secretHash(hasher.hash(generatedKey.rawKey()))
                .secretLastFour(generatedKey.secretLastFour())
                .environment(generatedKey.environment())
                .scopes(requestedApiKey.getScopes())
                .createdByAccountId(accountId)
                .expiresAt(requestedApiKey.getExpiresAt())
                .build();

        return new CreatedProjectApiKey(
                repository.save(apiKey),
                generatedKey.rawKey()
        );
    }

    @Cacheable(
            cacheNames = PROJECT_API_KEYS_BY_PROJECT_ID,
            key = "#projectId + ':' + #accountId + ':' + #pagination.page + ':' + #pagination.size + ':' "
                    + "+ (#filter == null ? 'null' : #filter.createdBy)"
    )
    public PageResult<ProjectApiKeyDetails> findAllByProjectId(
            String projectId,
            String accountId,
            Pagination pagination,
            ProjectApiKeyFilter filter
    ) {
        ensureProjectExists(projectId);
        ensureCanRead(projectId, accountId);

        PageResult<ProjectApiKey> apiKeys = repository.findAllByProjectId(projectId, pagination, filter);

        return new PageResult<>(
                apiKeys.content().stream()
                        .map(this::toDetails)
                        .toList(),
                apiKeys.page(),
                apiKeys.size(),
                apiKeys.totalElements(),
                apiKeys.totalPages()
        );
    }

    @Cacheable(
            cacheNames = PROJECT_API_KEY_BY_ID,
            key = "#projectId + ':' + #apiKeyId + ':' + #accountId"
    )
    public ProjectApiKeyDetails findById(String projectId, String apiKeyId, String accountId) {
        ensureProjectExists(projectId);
        ensureCanRead(projectId, accountId);

        return toDetails(findApiKeyOrThrow(projectId, apiKeyId));
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(
                    cacheNames = PROJECT_API_KEY_BY_ID,
                    allEntries = true
            ),
            @CacheEvict(
                    cacheNames = PROJECT_API_KEYS_BY_PROJECT_ID,
                    allEntries = true
            )
    })
    public void delete(String projectId, String apiKeyId, String accountId) {
        ensureProjectExists(projectId);
        ensureCanManage(projectId, accountId);

        ProjectApiKey apiKey = findApiKeyOrThrow(projectId, apiKeyId);

        repository.deleteById(apiKey.getId());
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(
                    cacheNames = PROJECT_API_KEY_BY_ID,
                    allEntries = true
            ),
            @CacheEvict(
                    cacheNames = PROJECT_API_KEYS_BY_PROJECT_ID,
                    allEntries = true
            )
    })
    public ProjectApiKey revoke(String projectId, String apiKeyId, String accountId) {
        ensureProjectExists(projectId);
        ensureCanManage(projectId, accountId);

        ProjectApiKey apiKey = findApiKeyOrThrow(projectId, apiKeyId);

        if (apiKey.isRevoked()) {
            throw new ProjectApiKeyAlreadyRevokedException();
        }

        apiKey.revoke();

        return repository.save(apiKey);
    }

    private ProjectApiKey findApiKeyOrThrow(String projectId, String apiKeyId) {
        return repository
                .findByProjectIdAndId(projectId, apiKeyId)
                .orElseThrow(ProjectApiKeyNotFoundException::new);
    }

    private ProjectApiKeyDetails toDetails(ProjectApiKey apiKey) {
        Account createdByAccount = accountRepository
                .findById(apiKey.getCreatedByAccountId())
                .orElseThrow(AccountNotFoundException::new);

        return new ProjectApiKeyDetails(apiKey, createdByAccount);
    }

    private void ensureCanCreate(ProjectApiKey apiKey) {
        if (apiKey.getName() == null || apiKey.getName().isBlank()) {
            throw new InvalidProjectApiKeyException("O nome da API key é obrigatório.");
        }

        if (apiKey.getScopes() == null || apiKey.getScopes().isEmpty()) {
            throw new InvalidProjectApiKeyException("Informe ao menos uma permissão para a API key.");
        }

        if (apiKey.getScopes().contains(null)) {
            throw new InvalidProjectApiKeyException("A permissão da API key não pode ser nula.");
        }

        if (apiKey.getExpiresAt() != null && !apiKey.getExpiresAt().isAfter(LocalDateTime.now())) {
            throw new InvalidProjectApiKeyException("A expiração da API key deve ser uma data futura.");
        }
    }

    private void ensureProjectExists(String projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ProjectNotFoundException("Projeto não encontrado: " + projectId);
        }
    }

    private void ensureCanRead(String projectId, String accountId) {
        if (!projectMemberRepository.existsByProjectIdAndAccountId(projectId, accountId)) {
            throw new ProjectAccessDeniedException();
        }
    }

    private void ensureCanManage(String projectId, String accountId) {
        if (!projectMemberRepository.existsByProjectIdAndAccountIdAndRoleIn(
                projectId,
                accountId,
                PROJECT_API_KEY_MANAGEMENT_ROLES
        )) {
            throw new ProjectAccessDeniedException();
        }
    }
}
