package com.samuelmaia1_github.yourauth.domain.project.authconfig;

import com.samuelmaia1_github.yourauth.domain.project.ProjectRepository;
import com.samuelmaia1_github.yourauth.domain.project.exceptions.ProjectAccessDeniedException;
import com.samuelmaia1_github.yourauth.domain.project.exceptions.ProjectNotFoundException;
import com.samuelmaia1_github.yourauth.domain.project.authconfig.exceptions.AuthConfigNotFoundException;
import com.samuelmaia1_github.yourauth.domain.project.authconfig.exceptions.InvalidAuthConfigException;
import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMemberRepository;
import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMemberRole;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthConfigService {
    private static final List<ProjectMemberRole> AUTH_CONFIG_MANAGEMENT_ROLES = List.of(
            ProjectMemberRole.OWNER,
            ProjectMemberRole.ADMIN
    );

    private final AuthConfigRepository repository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public AuthConfig findByProjectId(String projectId, String accountId) {
        ensureProjectExists(projectId);
        ensureCanRead(projectId, accountId);

        return findConfigOrThrow(projectId);
    }

    @Transactional
    public AuthConfig update(String projectId, AuthConfig requestedConfig, String accountId) {
        ensureProjectExists(projectId);
        ensureCanManage(projectId, accountId);

        AuthConfig currentConfig = findConfigOrThrow(projectId);
        AuthConfig updatedConfig = AuthConfig.builder()
                .id(currentConfig.getId())
                .projectId(currentConfig.getProjectId())
                .accessTokenExpirationMinutes(requestedConfig.getAccessTokenExpirationMinutes())
                .refreshTokenExpirationDays(requestedConfig.getRefreshTokenExpirationDays())
                .sessionMode(requestedConfig.getSessionMode())
                .maxActiveSessions(requestedConfig.getMaxActiveSessions())
                .refreshTokenRotationEnabled(requestedConfig.isRefreshTokenRotationEnabled())
                .revokeTokensOnPasswordChange(requestedConfig.isRevokeTokensOnPasswordChange())
                .failedLoginAttemptsLimit(requestedConfig.getFailedLoginAttemptsLimit())
                .lockDurationMinutes(requestedConfig.getLockDurationMinutes())
                .requireEmailVerification(requestedConfig.isRequireEmailVerification())
                .registrationEnabled(requestedConfig.isRegistrationEnabled())
                .createdAt(currentConfig.getCreatedAt())
                .updatedAt(currentConfig.getUpdatedAt())
                .build();

        ensureValid(updatedConfig);

        return repository.save(updatedConfig);
    }

    private AuthConfig findConfigOrThrow(String projectId) {
        return repository
                .findByProjectId(projectId)
                .orElseThrow(AuthConfigNotFoundException::new);
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
                AUTH_CONFIG_MANAGEMENT_ROLES
        )) {
            throw new ProjectAccessDeniedException();
        }
    }

    private void ensureValid(AuthConfig config) {
        if (
                SessionMode.LIMITED_ACTIVE_SESSIONS.equals(config.getSessionMode())
                        && config.getMaxActiveSessions() == null
        ) {
            throw new InvalidAuthConfigException(
                    "O limite de sessões ativas é obrigatório para o modo LIMITED_ACTIVE_SESSIONS."
            );
        }

        if (
                !SessionMode.LIMITED_ACTIVE_SESSIONS.equals(config.getSessionMode())
                        && config.getMaxActiveSessions() != null
        ) {
            throw new InvalidAuthConfigException(
                    "O limite de sessões ativas só deve ser informado para o modo LIMITED_ACTIVE_SESSIONS."
            );
        }
    }
}
