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
import com.samuelmaia1_github.yourauth.infra.interfaces.IPasswordEncoder;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

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
    public User create(User user, String accountId) {
        ensureProjectExists(user.getProjectId());
        ensureCanManage(user.getProjectId(), accountId);

        return create(user);
    }

    @Transactional
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

    public User findById(String projectId, String userId, String accountId) {
        ensureProjectExists(projectId);
        ensureCanRead(projectId, accountId);

        return findUserOrThrow(projectId, userId);
    }

    @Transactional
    public User update(String projectId, String userId, User user, String accountId) {
        ensureProjectExists(projectId);
        ensureCanManage(projectId, accountId);

        User currentUser = findUserOrThrow(projectId, userId);
        User updatedUser = User.builder()
                .id(currentUser.getId())
                .projectId(currentUser.getProjectId())
                .email(user.getEmail())
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
                .phone(user.getPhone())
                .build();

        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            updatedUser.updatePassword(encoder.encode(user.getPassword()));
        }

        policy.ensureCanUpdate(updatedUser);

        return userRepository.save(updatedUser);
    }

    @Transactional
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
}
