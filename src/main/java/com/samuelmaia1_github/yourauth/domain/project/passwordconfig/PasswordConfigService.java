package com.samuelmaia1_github.yourauth.domain.project.passwordconfig;

import com.samuelmaia1_github.yourauth.domain.project.ProjectRepository;
import com.samuelmaia1_github.yourauth.domain.project.exceptions.ProjectAccessDeniedException;
import com.samuelmaia1_github.yourauth.domain.project.exceptions.ProjectNotFoundException;
import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMemberRepository;
import com.samuelmaia1_github.yourauth.infra.cache.names.PasswordConfigCacheNames;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PasswordConfigService {
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
}
