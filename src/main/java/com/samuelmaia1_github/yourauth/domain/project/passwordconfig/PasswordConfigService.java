package com.samuelmaia1_github.yourauth.domain.project.passwordconfig;

import com.samuelmaia1_github.yourauth.domain.project.ProjectRepository;
import com.samuelmaia1_github.yourauth.domain.project.exceptions.ProjectAccessDeniedException;
import com.samuelmaia1_github.yourauth.domain.project.exceptions.ProjectNotFoundException;
import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMemberRepository;
import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMemberRole;
import com.samuelmaia1_github.yourauth.infra.cache.names.PasswordConfigCacheNames;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PasswordConfigService {
    private static final List<ProjectMemberRole> PASSWORD_CONFIG_MANAGEMENT_ROLES = List.of(
            ProjectMemberRole.OWNER,
            ProjectMemberRole.ADMIN
    );

    private final PasswordConfigRepository repository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;

    @Cacheable(
            cacheNames = PasswordConfigCacheNames.PASSWORD_CONFIG_BY_PROJECT_ID,
            key = "#projectId"
    )
    public PasswordConfig findByProjectId(String projectId) {
        return findConfigOrThrow(projectId);
    }

    @Cacheable(
            cacheNames = PasswordConfigCacheNames.PASSWORD_CONFIG_BY_PROJECT_AND_ACCOUNT,
            key = "#projectId + ':' + #accountId"
    )
    public PasswordConfig findByProjectId(String projectId, String accountId) {
        ensureProjectExists(projectId);
        ensureCanRead(projectId, accountId);

        return findConfigOrThrow(projectId);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(
                    cacheNames = PasswordConfigCacheNames.PASSWORD_CONFIG_BY_PROJECT_ID,
                    key = "#projectId"
            ),
            @CacheEvict(
                    cacheNames = PasswordConfigCacheNames.PASSWORD_CONFIG_BY_PROJECT_AND_ACCOUNT,
                    allEntries = true
            )
    })
    public PasswordConfig update(String projectId, PasswordConfig requestedConfig, String accountId) {
        ensureProjectExists(projectId);
        ensureCanManage(projectId, accountId);

        PasswordConfig currentConfig = findConfigOrThrow(projectId);
        PasswordConfig updatedConfig = PasswordConfig.builder()
                .id(currentConfig.getId())
                .projectId(currentConfig.getProjectId())
                .numberRequired(requestedConfig.isNumberRequired())
                .specialCharRequired(requestedConfig.isSpecialCharRequired())
                .uppercaseRequired(requestedConfig.isUppercaseRequired())
                .lowercaseRequired(requestedConfig.isLowercaseRequired())
                .minSize(requestedConfig.getMinSize())
                .maxSize(requestedConfig.getMaxSize())
                .build();

        ensureValid(updatedConfig);

        return repository.save(updatedConfig);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(
                    cacheNames = PasswordConfigCacheNames.PASSWORD_CONFIG_BY_PROJECT_ID,
                    key = "#projectId"
            ),
            @CacheEvict(
                    cacheNames = PasswordConfigCacheNames.PASSWORD_CONFIG_BY_PROJECT_AND_ACCOUNT,
                    allEntries = true
            )
    })
    public void delete(String projectId, String accountId) {
        ensureProjectExists(projectId);
        ensureCanManage(projectId, accountId);
        findConfigOrThrow(projectId);

        repository.deleteByProjectId(projectId);
    }

    private PasswordConfig findConfigOrThrow(String projectId) {
        return repository
                .findByProjectId(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(
                        "Configuração de senha não encontrada para o projeto: " + projectId
                ));
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
                PASSWORD_CONFIG_MANAGEMENT_ROLES
        )) {
            throw new ProjectAccessDeniedException();
        }
    }

    private void ensureValid(PasswordConfig config) {
        if (config.getMinSize() > config.getMaxSize()) {
            throw new IllegalArgumentException("O tamanho mínimo não pode ser maior que o tamanho máximo.");
        }

        if (
                config.getMinSize() < PasswordConfig.DEFAULT_MIN_SIZE
                        || config.getMaxSize() > PasswordConfig.DEFAULT_MAX_SIZE
        ) {
            throw new IllegalArgumentException("Os tamanhos de senha devem estar entre 1 e 120.");
        }
    }
}
