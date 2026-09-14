package com.samuelmaia1_github.yourauth.project.unit;

import com.samuelmaia1_github.yourauth.domain.project.Project;
import com.samuelmaia1_github.yourauth.domain.project.ProjectRepository;
import com.samuelmaia1_github.yourauth.domain.project.authconfig.AuthConfig;
import com.samuelmaia1_github.yourauth.domain.project.authconfig.AuthConfigRepository;
import com.samuelmaia1_github.yourauth.domain.project.authconfig.AuthConfigService;
import com.samuelmaia1_github.yourauth.domain.project.authconfig.AuthConfigUpdate;
import com.samuelmaia1_github.yourauth.domain.project.authconfig.SessionMode;
import com.samuelmaia1_github.yourauth.domain.project.authconfig.exceptions.InvalidAuthConfigException;
import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMember;
import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMemberRepository;
import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMemberRole;
import com.samuelmaia1_github.yourauth.domain.shared.PageResult;
import com.samuelmaia1_github.yourauth.domain.shared.Pagination;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthConfigServiceTest {
    private static final String PROJECT_ID = "project-id";
    private static final String ACCOUNT_ID = "account-id";

    @Test
    void shouldUpdateOnlyProvidedAuthConfigFields() {
        RecordingAuthConfigRepository repository = new RecordingAuthConfigRepository(Optional.of(currentConfig()));
        AuthConfigService service = new AuthConfigService(
                repository,
                new StubProjectRepository(true),
                new RecordingProjectMemberRepository(true)
        );

        AuthConfig updatedConfig = service.update(
                PROJECT_ID,
                new AuthConfigUpdate(
                        null,
                        false,
                        30,
                        true,
                        SessionMode.SINGLE_ACTIVE_SESSION,
                        true,
                        null,
                        true,
                        null,
                        false,
                        false,
                        true,
                        null,
                        false,
                        null,
                        false,
                        true,
                        true,
                        null,
                        false
                ),
                ACCOUNT_ID
        );

        assertThat(updatedConfig.getAccessTokenExpirationMinutes()).isEqualTo(15);
        assertThat(updatedConfig.getRefreshTokenExpirationDays()).isEqualTo(30);
        assertThat(updatedConfig.getSessionMode()).isEqualTo(SessionMode.SINGLE_ACTIVE_SESSION);
        assertThat(updatedConfig.getMaxActiveSessions()).isNull();
        assertThat(updatedConfig.isRefreshTokenRotationEnabled()).isTrue();
        assertThat(updatedConfig.isRevokeTokensOnPasswordChange()).isFalse();
        assertThat(updatedConfig.getFailedLoginAttemptsLimit()).isEqualTo(5);
        assertThat(updatedConfig.getLockDurationMinutes()).isEqualTo(15);
        assertThat(updatedConfig.isRequireEmailVerification()).isTrue();
        assertThat(updatedConfig.isRegistrationEnabled()).isTrue();
        assertThat(repository.savedConfig).isSameAs(updatedConfig);
    }

    @Test
    void shouldRejectInvalidMergedAuthConfig() {
        RecordingAuthConfigRepository repository = new RecordingAuthConfigRepository(Optional.of(currentConfig()));
        AuthConfigService service = new AuthConfigService(
                repository,
                new StubProjectRepository(true),
                new RecordingProjectMemberRepository(true)
        );

        assertThatThrownBy(() -> service.update(
                PROJECT_ID,
                new AuthConfigUpdate(
                        null,
                        false,
                        null,
                        false,
                        null,
                        false,
                        null,
                        true,
                        null,
                        false,
                        null,
                        false,
                        null,
                        false,
                        null,
                        false,
                        null,
                        false,
                        null,
                        false
                ),
                ACCOUNT_ID
        ))
                .isInstanceOf(InvalidAuthConfigException.class)
                .hasMessage("O limite de sessões ativas é obrigatório para o modo LIMITED_ACTIVE_SESSIONS.");

        assertThat(repository.savedConfig).isNull();
    }

    private static AuthConfig currentConfig() {
        return AuthConfig.builder()
                .id("auth-config-id")
                .projectId(PROJECT_ID)
                .accessTokenExpirationMinutes(15)
                .refreshTokenExpirationDays(7)
                .sessionMode(SessionMode.LIMITED_ACTIVE_SESSIONS)
                .maxActiveSessions(5)
                .refreshTokenRotationEnabled(true)
                .revokeTokensOnPasswordChange(true)
                .failedLoginAttemptsLimit(5)
                .lockDurationMinutes(15)
                .requireEmailVerification(false)
                .registrationEnabled(true)
                .build();
    }

    private static class RecordingAuthConfigRepository implements AuthConfigRepository {
        private final Optional<AuthConfig> currentConfig;
        private AuthConfig savedConfig;

        private RecordingAuthConfigRepository(Optional<AuthConfig> currentConfig) {
            this.currentConfig = currentConfig;
        }

        @Override
        public AuthConfig save(AuthConfig config) {
            savedConfig = config;
            return config;
        }

        @Override
        public Optional<AuthConfig> findByProjectId(String projectId) {
            return currentConfig;
        }
    }

    private static class StubProjectRepository implements ProjectRepository {
        private final boolean exists;

        private StubProjectRepository(boolean exists) {
            this.exists = exists;
        }

        @Override
        public Project save(Project project) {
            return project;
        }

        @Override
        public Optional<Project> findById(String id) {
            return exists ? Optional.of(Project.builder().id(id).build()) : Optional.empty();
        }

        @Override
        public boolean existsByOwnerAccountIdAndName(String ownerAccountId, String name) {
            return false;
        }

        @Override
        public boolean existsByOwnerAccountIdAndNameAndIdNot(String ownerAccountId, String name, String id) {
            return false;
        }

        @Override
        public PageResult<Project> findAllByOwnerAccountId(String id, Pagination pagination) {
            return new PageResult<>(List.of(), pagination.page(), pagination.size(), 0, 0);
        }

        @Override
        public PageResult<Project> findAllByMemberAccountId(String accountId, Pagination pagination) {
            return new PageResult<>(List.of(), pagination.page(), pagination.size(), 0, 0);
        }

        @Override
        public void deleteById(String id) {
        }
    }

    private static class RecordingProjectMemberRepository implements ProjectMemberRepository {
        private final boolean canManage;

        private RecordingProjectMemberRepository(boolean canManage) {
            this.canManage = canManage;
        }

        @Override
        public ProjectMember save(ProjectMember projectMember) {
            return projectMember;
        }

        @Override
        public Optional<ProjectMember> findByProjectIdAndAccountId(String projectId, String accountId) {
            return Optional.empty();
        }

        @Override
        public boolean existsByProjectIdAndAccountId(String projectId, String accountId) {
            return false;
        }

        @Override
        public boolean existsByProjectIdAndAccountIdAndRoleIn(
                String projectId,
                String accountId,
                Collection<ProjectMemberRole> roles
        ) {
            return canManage;
        }

        @Override
        public void deleteAllByProjectId(String projectId) {
        }

        @Override
        public void deleteByProjectIdAndAccountId(String projectId, String accountId) {
        }
    }
}
