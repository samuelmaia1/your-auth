package com.samuelmaia1_github.yourauth.domain.user;

import com.samuelmaia1_github.yourauth.domain.project.exceptions.ProjectAccessDeniedException;
import com.samuelmaia1_github.yourauth.domain.project.ProjectRepository;
import com.samuelmaia1_github.yourauth.domain.project.exceptions.ProjectNotFoundException;
import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMemberRepository;
import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMemberRole;
import com.samuelmaia1_github.yourauth.domain.projectapikey.ProjectApiKeyScope;
import com.samuelmaia1_github.yourauth.domain.projectapikey.exceptions.ProjectApiKeyAccessDeniedException;
import com.samuelmaia1_github.yourauth.domain.shared.PageResult;
import com.samuelmaia1_github.yourauth.domain.shared.Pagination;
import com.samuelmaia1_github.yourauth.domain.user.exceptions.UserNotFoundException;
import com.samuelmaia1_github.yourauth.infra.cache.names.AccountCacheNames;
import com.samuelmaia1_github.yourauth.infra.interfaces.IPasswordEncoder;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

import static com.samuelmaia1_github.yourauth.infra.cache.names.UserCacheNames.*;

@Service
@RequiredArgsConstructor
public class UserService {
    private static final List<ProjectMemberRole> PROJECT_USER_MANAGEMENT_ROLES = List.of(
            ProjectMemberRole.OWNER,
            ProjectMemberRole.ADMIN
    );

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserPolicy policy;
    private final IPasswordEncoder encoder;

    @Transactional
    @Caching(evict = {
            @CacheEvict(
                    cacheNames = USERS_BY_PROJECT_ID,
                    allEntries = true
            ),
            @CacheEvict(
                    cacheNames = AccountCacheNames.ACCOUNT_SUMMARY_BY_ACCOUNT_ID,
                    allEntries = true
            ),
            @CacheEvict(
                    cacheNames = AccountCacheNames.ACCOUNT_USAGE_BY_OWNER_ACCOUNT_ID,
                    allEntries = true
            )
    })
    public User create(User user, String accountId) {
        ensureProjectExists(user.getProjectId());
        ensureCanManage(user.getProjectId(), accountId);

        return create(user);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(
                    cacheNames = USERS_BY_PROJECT_ID,
                    allEntries = true
            ),
            @CacheEvict(
                    cacheNames = AccountCacheNames.ACCOUNT_SUMMARY_BY_ACCOUNT_ID,
                    allEntries = true
            ),
            @CacheEvict(
                    cacheNames = AccountCacheNames.ACCOUNT_USAGE_BY_OWNER_ACCOUNT_ID,
                    allEntries = true
            )
    })
    public User createWithApiKey(User user, Set<ProjectApiKeyScope> scopes) {
        ensureCanCreateWithApiKey(scopes);
        ensureProjectExists(user.getProjectId());

        return create(user);
    }

    private User create(User user) {
        policy.ensureCanCreate(user);

        if (user.getStatus() == null) {
            user.activate();
        }

        user.updatePassword(encoder.encode(user.getPassword()));

        return userRepository.save(user);
    }

    private void ensureCanCreateWithApiKey(Set<ProjectApiKeyScope> scopes) {
        if (scopes == null || !scopes.contains(ProjectApiKeyScope.USERS_WRITE)) {
            throw new ProjectApiKeyAccessDeniedException(
                    "A API key precisa da permissão USERS_WRITE para cadastrar usuários."
            );
        }
    }

    @Cacheable(
            cacheNames = USERS_BY_PROJECT_ID,
            key = "#projectId + ':' + #accountId + ':' + #pagination.page + ':' + #pagination.size + ':' "
                    + "+ (#filter == null ? 'null' : #filter.email + ':' + #filter.status)"
    )
    public PageResult<User> findAllByProjectId(
            String projectId,
            String accountId,
            Pagination pagination,
            UserFilter filter
    ) {
        ensureProjectExists(projectId);
        ensureCanRead(projectId, accountId);

        return userRepository.findAllByProjectId(projectId, pagination, filter);
    }

    @Cacheable(
            cacheNames = USER_BY_ID,
            key = "#projectId + ':' + #userId + ':' + #accountId"
    )
    public User findById(String projectId, String userId, String accountId) {
        ensureProjectExists(projectId);
        ensureCanRead(projectId, accountId);

        return findUserOrThrow(projectId, userId);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(
                    cacheNames = USER_BY_ID,
                    allEntries = true
            ),
            @CacheEvict(
                    cacheNames = USERS_BY_PROJECT_ID,
                    allEntries = true
            )
    })
    public User update(String projectId, String userId, UserUpdate user, String accountId) {
        ensureProjectExists(projectId);
        ensureCanManage(projectId, accountId);

        User currentUser = findUserOrThrow(projectId, userId);
        User updatedUser = User.builder()
                .id(currentUser.getId())
                .projectId(currentUser.getProjectId())
                .email(requiredTextOrCurrent(
                        user.email(),
                        user.emailProvided(),
                        currentUser.getEmail(),
                        "O e-mail é obrigatório"
                ))
                .password(currentUser.getPassword())
                .status(currentUser.getStatus())
                .createdAt(currentUser.getCreatedAt())
                .updatedAt(currentUser.getUpdatedAt())
                .lastLoginAt(currentUser.getLastLoginAt())
                .lastPasswordChangedAt(currentUser.getLastPasswordChangedAt())
                .lastFailedLoginAt(currentUser.getLastFailedLoginAt())
                .failedLoginAttempts(currentUser.getFailedLoginAttempts())
                .lockedUntil(currentUser.getLockedUntil())
                .lastLoginIpAddress(currentUser.getLastLoginIpAddress())
                .lastLoginUserAgent(currentUser.getLastLoginUserAgent())
                .phone(user.phoneProvided() ? user.phone() : currentUser.getPhone())
                .build();

        if (user.passwordProvided()) {
            if (user.password() == null || user.password().isBlank()) {
                throw new IllegalArgumentException("A senha é obrigatória");
            }

            updatedUser.updatePassword(encoder.encode(user.password()));
        }

        policy.ensureCanUpdate(updatedUser);

        return userRepository.save(updatedUser);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(
                    cacheNames = USER_BY_ID,
                    allEntries = true
            ),
            @CacheEvict(
                    cacheNames = USERS_BY_PROJECT_ID,
                    allEntries = true
            ),
            @CacheEvict(
                    cacheNames = AccountCacheNames.ACCOUNT_SUMMARY_BY_ACCOUNT_ID,
                    allEntries = true
            ),
            @CacheEvict(
                    cacheNames = AccountCacheNames.ACCOUNT_USAGE_BY_OWNER_ACCOUNT_ID,
                    allEntries = true
            )
    })
    public void delete(String projectId, String userId, String accountId) {
        ensureProjectExists(projectId);
        ensureCanManage(projectId, accountId);

        User user = findUserOrThrow(projectId, userId);

        userRepository.deleteById(user.getId());
    }

    private User findUserOrThrow(String projectId, String userId) {
        return userRepository
                .findByProjectIdAndId(projectId, userId)
                .orElseThrow(UserNotFoundException::new);
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
                PROJECT_USER_MANAGEMENT_ROLES
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
}
