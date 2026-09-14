package com.samuelmaia1_github.yourauth.domain.project;

import com.samuelmaia1_github.yourauth.domain.account.AccountRepository;
import com.samuelmaia1_github.yourauth.domain.account.exceptions.AccountNotFoundException;
import com.samuelmaia1_github.yourauth.domain.project.exceptions.ProjectAccessDeniedException;
import com.samuelmaia1_github.yourauth.domain.project.exceptions.ProjectNotFoundException;
import com.samuelmaia1_github.yourauth.domain.project.authconfig.AuthConfig;
import com.samuelmaia1_github.yourauth.domain.project.authconfig.AuthConfigRepository;
import com.samuelmaia1_github.yourauth.domain.project.passwordconfig.PasswordConfig;
import com.samuelmaia1_github.yourauth.domain.project.passwordconfig.PasswordConfigRepository;
import com.samuelmaia1_github.yourauth.domain.shared.PageResult;
import com.samuelmaia1_github.yourauth.domain.shared.Pagination;
import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMember;
import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMemberRepository;
import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMemberRole;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.samuelmaia1_github.yourauth.infra.cache.names.AccountCacheNames.*;
import static com.samuelmaia1_github.yourauth.infra.cache.names.AuthConfigCacheNames.*;
import static com.samuelmaia1_github.yourauth.infra.cache.names.PasswordConfigCacheNames.*;
import static com.samuelmaia1_github.yourauth.infra.cache.names.ProjectApiKeyCacheNames.*;
import static com.samuelmaia1_github.yourauth.infra.cache.names.ProjectCacheNames.*;
import static com.samuelmaia1_github.yourauth.infra.cache.names.ProjectMemberCacheNames.*;
import static com.samuelmaia1_github.yourauth.infra.cache.names.UserCacheNames.*;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private static final List<ProjectMemberRole> PROJECT_MANAGEMENT_ROLES = List.of(
            ProjectMemberRole.OWNER,
            ProjectMemberRole.ADMIN
    );

    private final ProjectRepository repository;
    private final PasswordConfigRepository passwordConfigRepository;
    private final AuthConfigRepository authConfigRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final AccountRepository accountRepository;
    private final ProjectPolicy policy;

    @Transactional
    @Caching(evict = {
            @CacheEvict(
                    cacheNames = PROJECT_BY_ACCOUNT_ID,
                    allEntries = true
            ),
            @CacheEvict(
                    cacheNames = ACCOUNT_SUMMARY_BY_ACCOUNT_ID,
                    key = "#project.ownerAccountId"
            ),
            @CacheEvict(
                    cacheNames = ACCOUNT_USAGE_BY_OWNER_ACCOUNT_ID,
                    key = "#project.ownerAccountId"
            )
    })
    public Project create(Project project, PasswordConfig requestedPasswordConfig) {
        return create(project, requestedPasswordConfig, null);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(
                    cacheNames = PROJECT_BY_ACCOUNT_ID,
                    allEntries = true
            ),
            @CacheEvict(
                    cacheNames = ACCOUNT_SUMMARY_BY_ACCOUNT_ID,
                    key = "#project.ownerAccountId"
            ),
            @CacheEvict(
                    cacheNames = ACCOUNT_USAGE_BY_OWNER_ACCOUNT_ID,
                    key = "#project.ownerAccountId"
            )
    })
    public Project create(Project project, PasswordConfig requestedPasswordConfig, AuthConfig requestedAuthConfig) {
        accountRepository.findById(project.getOwnerAccountId())
                .orElseThrow(AccountNotFoundException::new);

        policy.ensureCanCreate(project);

        Project createdProject = repository.save(project);

        projectMemberRepository.save(ProjectMember.builder()
                .projectId(createdProject.getId())
                .accountId(createdProject.getOwnerAccountId())
                .role(ProjectMemberRole.OWNER)
                .build());

        PasswordConfig passwordConfig =
                requestedPasswordConfig == null ? PasswordConfig.createDefault() : requestedPasswordConfig;

        passwordConfig.assignToProject(createdProject.getId());

        passwordConfigRepository.save(passwordConfig);

        AuthConfig authConfig =
                requestedAuthConfig == null ? AuthConfig.createDefault() : requestedAuthConfig;

        authConfig.assignToProject(createdProject.getId());

        authConfigRepository.save(authConfig);

        return createdProject;
    }

    @Cacheable(
            cacheNames = PROJECT_BY_ID,
            key = "#id + ':' + #accountId"
    )
    public Project findById(String id, String accountId) {
        Project project = findProjectOrThrow(id);
        ensureCanRead(project.getId(), accountId);

        return project;
    }

    @Cacheable(
            cacheNames = PROJECT_BY_ACCOUNT_ID,
            key = "#accountId + ':' + #pagination.page + ':' + #pagination.size"
    )
    public PageResult<Project> findAllByAccountId(String accountId, Pagination pagination) {
        return repository.findAllByMemberAccountId(accountId, pagination);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(
                    cacheNames = PROJECT_BY_ID,
                    allEntries = true
            ),
            @CacheEvict(
                    cacheNames = PROJECT_BY_ACCOUNT_ID,
                    allEntries = true
            ),
            @CacheEvict(
                    cacheNames = ACCOUNT_SUMMARY_BY_ACCOUNT_ID,
                    allEntries = true
            )
    })
    public Project update(String id, ProjectUpdate project, String accountId) {
        Project currentProject = findProjectOrThrow(id);
        ensureCanManage(currentProject.getId(), accountId);

        Project updatedProject = Project.builder()
                .id(currentProject.getId())
                .name(requiredTextOrCurrent(
                        project.name(),
                        project.nameProvided(),
                        currentProject.getName(),
                        "O nome é obrigatório"
                ))
                .description(project.descriptionProvided() ? project.description() : currentProject.getDescription())
                .ownerAccountId(currentProject.getOwnerAccountId())
                .status(requiredValueOrCurrent(
                        project.status(),
                        project.statusProvided(),
                        currentProject.getStatus(),
                        "O status é obrigatório"
                ))
                .environment(requiredValueOrCurrent(
                        project.environment(),
                        project.environmentProvided(),
                        currentProject.getEnvironment(),
                        "O ambiente é obrigatório"
                ))
                .tokenAudience(requiredTextOrCurrent(
                        project.tokenAudience(),
                        project.tokenAudienceProvided(),
                        currentProject.getTokenAudience(),
                        "A audiência do token é obrigatória"
                ))
                .createdAt(currentProject.getCreatedAt())
                .updatedAt(currentProject.getUpdatedAt())
                .build();

        policy.ensureCanUpdate(updatedProject);

        return repository.save(updatedProject);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(
                    cacheNames = PROJECT_BY_ID,
                    allEntries = true
            ),
            @CacheEvict(
                    cacheNames = PROJECT_BY_ACCOUNT_ID,
                    allEntries = true
            ),
            @CacheEvict(
                    cacheNames = ACCOUNT_SUMMARY_BY_ACCOUNT_ID,
                    allEntries = true
            ),
            @CacheEvict(
                    cacheNames = ACCOUNT_USAGE_BY_OWNER_ACCOUNT_ID,
                    allEntries = true
            ),
            @CacheEvict(
                    cacheNames = AUTH_CONFIG_BY_PROJECT_AND_ACCOUNT,
                    allEntries = true
            ),
            @CacheEvict(
                    cacheNames = PASSWORD_CONFIG_BY_PROJECT_ID,
                    key = "#id"
            ),
            @CacheEvict(
                    cacheNames = PASSWORD_CONFIG_BY_PROJECT_AND_ACCOUNT,
                    allEntries = true
            ),
            @CacheEvict(
                    cacheNames = PROJECT_MEMBERS_BY_PROJECT_ID,
                    allEntries = true
            ),
            @CacheEvict(
                    cacheNames = USER_BY_ID,
                    allEntries = true
            ),
            @CacheEvict(
                    cacheNames = USERS_BY_PROJECT_ID,
                    allEntries = true
            ),
            @CacheEvict(
                    cacheNames = PROJECT_API_KEY_BY_ID,
                    allEntries = true
            ),
            @CacheEvict(
                    cacheNames = PROJECT_API_KEYS_BY_PROJECT_ID,
                    allEntries = true
            )
    })
    public void delete(String id, String accountId) {
        Project project = findProjectOrThrow(id);
        ensureCanManage(project.getId(), accountId);

        projectMemberRepository.deleteAllByProjectId(project.getId());
        repository.deleteById(project.getId());
    }

    private Project findProjectOrThrow(String id) {
        return repository.findById(id).orElseThrow(ProjectNotFoundException::new);
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
                PROJECT_MANAGEMENT_ROLES
        )) {
            throw new ProjectAccessDeniedException();
        }
    }

    private static String requiredTextOrCurrent(String value, boolean provided, String currentValue, String message) {
        if (!provided) {
            return currentValue;
        }

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }

        return value;
    }

    private static <T> T requiredValueOrCurrent(T value, boolean provided, T currentValue, String message) {
        if (!provided) {
            return currentValue;
        }

        if (value == null) {
            throw new IllegalArgumentException(message);
        }

        return value;
    }
}
