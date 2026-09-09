package com.samuelmaia1_github.yourauth.project.unit;

import com.samuelmaia1_github.yourauth.domain.project.Project;
import com.samuelmaia1_github.yourauth.domain.project.ProjectRepository;
import com.samuelmaia1_github.yourauth.domain.project.exceptions.ProjectAccessDeniedException;
import com.samuelmaia1_github.yourauth.domain.project.exceptions.ProjectNotFoundException;
import com.samuelmaia1_github.yourauth.domain.project.passwordconfig.PasswordConfig;
import com.samuelmaia1_github.yourauth.domain.project.passwordconfig.PasswordConfigRepository;
import com.samuelmaia1_github.yourauth.domain.project.passwordconfig.PasswordConfigService;
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

class PasswordConfigServiceTest {
    private static final String PROJECT_ID = "project-id";
    private static final String ACCOUNT_ID = "account-id";

    @Test
    void shouldFindPasswordConfigWhenAuthenticatedAccountIsProjectMember() {
        RecordingPasswordConfigRepository passwordConfigRepository = new RecordingPasswordConfigRepository(
                Optional.of(currentConfig())
        );
        RecordingProjectMemberRepository memberRepository = new RecordingProjectMemberRepository(true, false);
        PasswordConfigService service = new PasswordConfigService(
                passwordConfigRepository,
                new StubProjectRepository(true),
                memberRepository
        );

        PasswordConfig config = service.findByProjectId(PROJECT_ID, ACCOUNT_ID);

        assertThat(config.getId()).isEqualTo("password-config-id");
        assertThat(memberRepository.readProjectId).isEqualTo(PROJECT_ID);
        assertThat(memberRepository.readAccountId).isEqualTo(ACCOUNT_ID);
    }

    @Test
    void shouldUpdatePasswordConfigWhenAuthenticatedAccountCanManageProject() {
        RecordingPasswordConfigRepository passwordConfigRepository = new RecordingPasswordConfigRepository(
                Optional.of(currentConfig())
        );
        RecordingProjectMemberRepository memberRepository = new RecordingProjectMemberRepository(true, true);
        PasswordConfigService service = new PasswordConfigService(
                passwordConfigRepository,
                new StubProjectRepository(true),
                memberRepository
        );
        PasswordConfig requestedConfig = PasswordConfig.builder()
                .minSize(10)
                .maxSize(80)
                .numberRequired(true)
                .uppercaseRequired(true)
                .lowercaseRequired(true)
                .specialCharRequired(true)
                .build();

        PasswordConfig updatedConfig = service.update(PROJECT_ID, requestedConfig, ACCOUNT_ID);

        assertThat(updatedConfig.getId()).isEqualTo("password-config-id");
        assertThat(updatedConfig.getProjectId()).isEqualTo(PROJECT_ID);
        assertThat(updatedConfig.getMinSize()).isEqualTo(10);
        assertThat(updatedConfig.getMaxSize()).isEqualTo(80);
        assertThat(updatedConfig.isNumberRequired()).isTrue();
        assertThat(passwordConfigRepository.savedConfig).isSameAs(updatedConfig);
        assertThat(memberRepository.managementRoles).containsExactly(ProjectMemberRole.OWNER, ProjectMemberRole.ADMIN);
    }

    @Test
    void shouldDenyUpdatePasswordConfigWhenAuthenticatedAccountCannotManageProject() {
        RecordingPasswordConfigRepository passwordConfigRepository = new RecordingPasswordConfigRepository(
                Optional.of(currentConfig())
        );
        PasswordConfigService service = new PasswordConfigService(
                passwordConfigRepository,
                new StubProjectRepository(true),
                new RecordingProjectMemberRepository(true, false)
        );

        assertThatThrownBy(() -> service.update(PROJECT_ID, currentConfig(), ACCOUNT_ID))
                .isInstanceOf(ProjectAccessDeniedException.class);

        assertThat(passwordConfigRepository.savedConfig).isNull();
    }

    @Test
    void shouldDeletePasswordConfigWhenAuthenticatedAccountCanManageProject() {
        RecordingPasswordConfigRepository passwordConfigRepository = new RecordingPasswordConfigRepository(
                Optional.of(currentConfig())
        );
        PasswordConfigService service = new PasswordConfigService(
                passwordConfigRepository,
                new StubProjectRepository(true),
                new RecordingProjectMemberRepository(true, true)
        );

        service.delete(PROJECT_ID, ACCOUNT_ID);

        assertThat(passwordConfigRepository.deletedProjectId).isEqualTo(PROJECT_ID);
    }

    @Test
    void shouldDenyDeletePasswordConfigWhenAuthenticatedAccountCannotManageProject() {
        RecordingPasswordConfigRepository passwordConfigRepository = new RecordingPasswordConfigRepository(
                Optional.of(currentConfig())
        );
        PasswordConfigService service = new PasswordConfigService(
                passwordConfigRepository,
                new StubProjectRepository(true),
                new RecordingProjectMemberRepository(true, false)
        );

        assertThatThrownBy(() -> service.delete(PROJECT_ID, ACCOUNT_ID))
                .isInstanceOf(ProjectAccessDeniedException.class);

        assertThat(passwordConfigRepository.deletedProjectId).isNull();
    }

    @Test
    void shouldRejectPasswordConfigWithInvalidRange() {
        RecordingPasswordConfigRepository passwordConfigRepository = new RecordingPasswordConfigRepository(
                Optional.of(currentConfig())
        );
        PasswordConfigService service = new PasswordConfigService(
                passwordConfigRepository,
                new StubProjectRepository(true),
                new RecordingProjectMemberRepository(true, true)
        );
        PasswordConfig requestedConfig = PasswordConfig.builder()
                .minSize(20)
                .maxSize(10)
                .build();

        assertThatThrownBy(() -> service.update(PROJECT_ID, requestedConfig, ACCOUNT_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O tamanho mínimo não pode ser maior que o tamanho máximo.");

        assertThat(passwordConfigRepository.savedConfig).isNull();
    }

    @Test
    void shouldThrowWhenProjectDoesNotExist() {
        RecordingPasswordConfigRepository passwordConfigRepository = new RecordingPasswordConfigRepository(
                Optional.of(currentConfig())
        );
        RecordingProjectMemberRepository memberRepository = new RecordingProjectMemberRepository(true, true);
        PasswordConfigService service = new PasswordConfigService(
                passwordConfigRepository,
                new StubProjectRepository(false),
                memberRepository
        );

        assertThatThrownBy(() -> service.findByProjectId(PROJECT_ID, ACCOUNT_ID))
                .isInstanceOf(ProjectNotFoundException.class)
                .hasMessage("Projeto não encontrado: " + PROJECT_ID);

        assertThat(memberRepository.readProjectId).isNull();
    }

    private static PasswordConfig currentConfig() {
        return PasswordConfig.builder()
                .id("password-config-id")
                .projectId(PROJECT_ID)
                .minSize(1)
                .maxSize(120)
                .build();
    }

    private static class RecordingPasswordConfigRepository implements PasswordConfigRepository {
        private final Optional<PasswordConfig> currentConfig;
        private PasswordConfig savedConfig;
        private String deletedProjectId;

        private RecordingPasswordConfigRepository(Optional<PasswordConfig> currentConfig) {
            this.currentConfig = currentConfig;
        }

        @Override
        public PasswordConfig save(PasswordConfig config) {
            savedConfig = config;
            return config;
        }

        @Override
        public Optional<PasswordConfig> findByProjectId(String projectId) {
            return currentConfig;
        }

        @Override
        public void deleteByProjectId(String projectId) {
            deletedProjectId = projectId;
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
        private final boolean member;
        private final boolean canManage;
        private String readProjectId;
        private String readAccountId;
        private Collection<ProjectMemberRole> managementRoles;

        private RecordingProjectMemberRepository(boolean member, boolean canManage) {
            this.member = member;
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
            readProjectId = projectId;
            readAccountId = accountId;
            return member;
        }

        @Override
        public boolean existsByProjectIdAndAccountIdAndRoleIn(
                String projectId,
                String accountId,
                Collection<ProjectMemberRole> roles
        ) {
            managementRoles = roles;
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
