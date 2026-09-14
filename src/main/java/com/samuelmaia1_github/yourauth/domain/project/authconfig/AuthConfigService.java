package com.samuelmaia1_github.yourauth.domain.project.authconfig;

import com.samuelmaia1_github.yourauth.domain.project.ProjectRepository;
import com.samuelmaia1_github.yourauth.domain.project.exceptions.ProjectAccessDeniedException;
import com.samuelmaia1_github.yourauth.domain.project.exceptions.ProjectNotFoundException;
import com.samuelmaia1_github.yourauth.domain.project.authconfig.exceptions.AuthConfigNotFoundException;
import com.samuelmaia1_github.yourauth.domain.project.authconfig.exceptions.InvalidAuthConfigException;
import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMemberRepository;
import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMemberRole;
import com.samuelmaia1_github.yourauth.infra.cache.names.AuthConfigCacheNames;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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

    @Cacheable(
            cacheNames = AuthConfigCacheNames.AUTH_CONFIG_BY_PROJECT_AND_ACCOUNT,
            key = "#projectId + ':' + #accountId"
    )
    public AuthConfig findByProjectId(String projectId, String accountId) {
        ensureProjectExists(projectId);
        ensureCanRead(projectId, accountId);

        return findConfigOrThrow(projectId);
    }

    @Transactional
    @CacheEvict(
            cacheNames = AuthConfigCacheNames.AUTH_CONFIG_BY_PROJECT_AND_ACCOUNT,
            allEntries = true
    )
    public AuthConfig update(String projectId, AuthConfigUpdate requestedConfig, String accountId) {
        ensureProjectExists(projectId);
        ensureCanManage(projectId, accountId);

        AuthConfig currentConfig = findConfigOrThrow(projectId);
        AuthConfig updatedConfig = AuthConfig.builder()
                .id(currentConfig.getId())
                .projectId(currentConfig.getProjectId())
                .accessTokenExpirationMinutes(requiredIntOrCurrent(
                        requestedConfig.accessTokenExpirationMinutes(),
                        requestedConfig.accessTokenExpirationMinutesProvided(),
                        currentConfig.getAccessTokenExpirationMinutes(),
                        "A expiração do access token é obrigatória."
                ))
                .refreshTokenExpirationDays(requiredIntOrCurrent(
                        requestedConfig.refreshTokenExpirationDays(),
                        requestedConfig.refreshTokenExpirationDaysProvided(),
                        currentConfig.getRefreshTokenExpirationDays(),
                        "A expiração do refresh token é obrigatória."
                ))
                .sessionMode(requiredValueOrCurrent(
                        requestedConfig.sessionMode(),
                        requestedConfig.sessionModeProvided(),
                        currentConfig.getSessionMode(),
                        "O modo de sessão é obrigatório."
                ))
                .maxActiveSessions(
                        requestedConfig.maxActiveSessionsProvided()
                                ? requestedConfig.maxActiveSessions()
                                : currentConfig.getMaxActiveSessions()
                )
                .refreshTokenRotationEnabled(requiredBooleanOrCurrent(
                        requestedConfig.refreshTokenRotationEnabled(),
                        requestedConfig.refreshTokenRotationEnabledProvided(),
                        currentConfig.isRefreshTokenRotationEnabled(),
                        "A rotação de refresh token é obrigatória."
                ))
                .revokeTokensOnPasswordChange(requiredBooleanOrCurrent(
                        requestedConfig.revokeTokensOnPasswordChange(),
                        requestedConfig.revokeTokensOnPasswordChangeProvided(),
                        currentConfig.isRevokeTokensOnPasswordChange(),
                        "A revogação de tokens na troca de senha é obrigatória."
                ))
                .failedLoginAttemptsLimit(requiredIntOrCurrent(
                        requestedConfig.failedLoginAttemptsLimit(),
                        requestedConfig.failedLoginAttemptsLimitProvided(),
                        currentConfig.getFailedLoginAttemptsLimit(),
                        "O limite de tentativas de login é obrigatório."
                ))
                .lockDurationMinutes(requiredIntOrCurrent(
                        requestedConfig.lockDurationMinutes(),
                        requestedConfig.lockDurationMinutesProvided(),
                        currentConfig.getLockDurationMinutes(),
                        "A duração do bloqueio é obrigatória."
                ))
                .requireEmailVerification(requiredBooleanOrCurrent(
                        requestedConfig.requireEmailVerification(),
                        requestedConfig.requireEmailVerificationProvided(),
                        currentConfig.isRequireEmailVerification(),
                        "A exigência de verificação de e-mail é obrigatória."
                ))
                .registrationEnabled(requiredBooleanOrCurrent(
                        requestedConfig.registrationEnabled(),
                        requestedConfig.registrationEnabledProvided(),
                        currentConfig.isRegistrationEnabled(),
                        "A habilitação de cadastro é obrigatória."
                ))
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

    private static int requiredIntOrCurrent(Integer value, boolean provided, int currentValue, String message) {
        if (!provided) {
            return currentValue;
        }

        if (value == null) {
            throw new IllegalArgumentException(message);
        }

        return value;
    }

    private static boolean requiredBooleanOrCurrent(
            Boolean value,
            boolean provided,
            boolean currentValue,
            String message
    ) {
        if (!provided) {
            return currentValue;
        }

        if (value == null) {
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
