package com.samuelmaia1_github.yourauth.project.unit;

import com.samuelmaia1_github.yourauth.domain.account.Account;
import com.samuelmaia1_github.yourauth.domain.account.AccountRepository;
import com.samuelmaia1_github.yourauth.domain.account.exceptions.AccountNotFoundException;
import com.samuelmaia1_github.yourauth.domain.project.Project;
import com.samuelmaia1_github.yourauth.domain.project.ProjectEnvironment;
import com.samuelmaia1_github.yourauth.domain.project.ProjectPolicy;
import com.samuelmaia1_github.yourauth.domain.project.ProjectRepository;
import com.samuelmaia1_github.yourauth.domain.project.ProjectService;
import com.samuelmaia1_github.yourauth.domain.project.ProjectStatus;
import com.samuelmaia1_github.yourauth.domain.project.exceptions.ProjectAccessDeniedException;
import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMember;
import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMemberRepository;
import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMemberRole;
import com.samuelmaia1_github.yourauth.domain.shared.PageResult;
import com.samuelmaia1_github.yourauth.domain.shared.Pagination;
import com.samuelmaia1_github.yourauth.domain.valueobjects.CPF;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ProjectServiceTest {
    @Test
    void shouldCreateProjectAndAddOwnerAsProjectMember() {
        Project project = project();
        RecordingProjectRepository projectRepository = new RecordingProjectRepository();
        RecordingProjectMemberRepository projectMemberRepository = new RecordingProjectMemberRepository();
        RecordingProjectPolicy policy = new RecordingProjectPolicy();
        ProjectService service = new ProjectService(
                projectRepository,
                projectMemberRepository,
                new StubAccountRepository(Optional.of(Account.builder().id("account-id").build())),
                policy
        );

        Project createdProject = service.create(project);

        assertThat(policy.checkedCreateProject).isSameAs(project);
        assertThat(projectRepository.savedProject).isSameAs(project);
        assertThat(createdProject.getId()).isEqualTo("project-id");
        assertThat(projectMemberRepository.savedProjectMember.getProjectId()).isEqualTo("project-id");
        assertThat(projectMemberRepository.savedProjectMember.getAccountId()).isEqualTo("account-id");
        assertThat(projectMemberRepository.savedProjectMember.getRole()).isEqualTo(ProjectMemberRole.OWNER);
    }

    @Test
    void shouldThrowAccountNotFoundWhenOwnerAccountDoesNotExist() {
        RecordingProjectRepository projectRepository = new RecordingProjectRepository();
        RecordingProjectMemberRepository projectMemberRepository = new RecordingProjectMemberRepository();
        RecordingProjectPolicy policy = new RecordingProjectPolicy();
        ProjectService service = new ProjectService(
                projectRepository,
                projectMemberRepository,
                new StubAccountRepository(Optional.empty()),
                policy
        );

        assertThatThrownBy(() -> service.create(project()))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessage("Conta não encontrada.");

        assertThat(policy.checkedCreateProject).isNull();
        assertThat(projectRepository.savedProject).isNull();
        assertThat(projectMemberRepository.savedProjectMember).isNull();
    }

    @Test
    void shouldFindProjectWhenAuthenticatedAccountIsMember() {
        Project project = savedProject();
        RecordingProjectMemberRepository projectMemberRepository = new RecordingProjectMemberRepository();
        projectMemberRepository.member = true;
        ProjectService service = serviceWith(
                new RecordingProjectRepository(Optional.of(project)),
                projectMemberRepository,
                new RecordingProjectPolicy()
        );

        Project foundProject = service.findById("project-id", "member-account-id");

        assertThat(foundProject).isSameAs(project);
        assertThat(projectMemberRepository.readProjectId).isEqualTo("project-id");
        assertThat(projectMemberRepository.readAccountId).isEqualTo("member-account-id");
    }

    @Test
    void shouldDenyFindProjectWhenAuthenticatedAccountIsNotMember() {
        RecordingProjectMemberRepository projectMemberRepository = new RecordingProjectMemberRepository();
        projectMemberRepository.member = false;
        ProjectService service = serviceWith(
                new RecordingProjectRepository(Optional.of(savedProject())),
                projectMemberRepository,
                new RecordingProjectPolicy()
        );

        assertThatThrownBy(() -> service.findById("project-id", "account-id"))
                .isInstanceOf(ProjectAccessDeniedException.class)
                .hasMessage("A conta autenticada não tem permissão para acessar este projeto.");
    }

    @Test
    void shouldUpdateProjectWhenAuthenticatedAccountCanManageProject() {
        RecordingProjectRepository projectRepository = new RecordingProjectRepository(Optional.of(savedProject()));
        RecordingProjectMemberRepository projectMemberRepository = new RecordingProjectMemberRepository();
        projectMemberRepository.canManage = true;
        RecordingProjectPolicy policy = new RecordingProjectPolicy();
        ProjectService service = serviceWith(projectRepository, projectMemberRepository, policy);

        Project updatedProject = service.update("project-id", updateProject(), "admin-account-id");

        assertThat(policy.checkedUpdateProject).isSameAs(projectRepository.savedProject);
        assertThat(projectRepository.savedProject.getId()).isEqualTo("project-id");
        assertThat(projectRepository.savedProject.getName()).isEqualTo("Updated Project");
        assertThat(projectRepository.savedProject.getOwnerAccountId()).isEqualTo("owner-account-id");
        assertThat(updatedProject.getId()).isEqualTo("project-id");
        assertThat(projectMemberRepository.managementRoles).containsExactlyInAnyOrder(
                ProjectMemberRole.OWNER,
                ProjectMemberRole.ADMIN
        );
    }

    @Test
    void shouldDenyUpdateProjectWhenAuthenticatedAccountCannotManageProject() {
        RecordingProjectRepository projectRepository = new RecordingProjectRepository(Optional.of(savedProject()));
        RecordingProjectMemberRepository projectMemberRepository = new RecordingProjectMemberRepository();
        projectMemberRepository.canManage = false;
        RecordingProjectPolicy policy = new RecordingProjectPolicy();
        ProjectService service = serviceWith(projectRepository, projectMemberRepository, policy);

        assertThatThrownBy(() -> service.update("project-id", updateProject(), "viewer-account-id"))
                .isInstanceOf(ProjectAccessDeniedException.class);

        assertThat(policy.checkedUpdateProject).isNull();
        assertThat(projectRepository.savedProject).isNull();
    }

    @Test
    void shouldDeleteProjectWhenAuthenticatedAccountCanManageProject() {
        RecordingProjectRepository projectRepository = new RecordingProjectRepository(Optional.of(savedProject()));
        RecordingProjectMemberRepository projectMemberRepository = new RecordingProjectMemberRepository();
        projectMemberRepository.canManage = true;
        ProjectService service = serviceWith(
                projectRepository,
                projectMemberRepository,
                new RecordingProjectPolicy()
        );

        service.delete("project-id", "owner-account-id");

        assertThat(projectMemberRepository.deletedProjectId).isEqualTo("project-id");
        assertThat(projectRepository.deletedProjectId).isEqualTo("project-id");
    }

    @Test
    void shouldDenyDeleteProjectWhenAuthenticatedAccountCannotManageProject() {
        RecordingProjectRepository projectRepository = new RecordingProjectRepository(Optional.of(savedProject()));
        RecordingProjectMemberRepository projectMemberRepository = new RecordingProjectMemberRepository();
        projectMemberRepository.canManage = false;
        ProjectService service = serviceWith(
                projectRepository,
                projectMemberRepository,
                new RecordingProjectPolicy()
        );

        assertThatThrownBy(() -> service.delete("project-id", "viewer-account-id"))
                .isInstanceOf(ProjectAccessDeniedException.class);

        assertThat(projectMemberRepository.deletedProjectId).isNull();
        assertThat(projectRepository.deletedProjectId).isNull();
    }

    private ProjectService serviceWith(
            ProjectRepository projectRepository,
            ProjectMemberRepository projectMemberRepository,
            ProjectPolicy policy
    ) {
        return new ProjectService(
                projectRepository,
                projectMemberRepository,
                new StubAccountRepository(Optional.empty()),
                policy
        );
    }

    private Project project() {
        return Project.builder()
                .name("My Project")
                .description("Project description")
                .ownerAccountId("account-id")
                .status(ProjectStatus.ACTIVE)
                .environment(ProjectEnvironment.DEVELOPMENT)
                .tokenAudience("my-project")
                .build();
    }

    private Project savedProject() {
        return Project.builder()
                .id("project-id")
                .name("My Project")
                .description("Project description")
                .ownerAccountId("owner-account-id")
                .status(ProjectStatus.ACTIVE)
                .environment(ProjectEnvironment.DEVELOPMENT)
                .tokenAudience("my-project")
                .createdAt(LocalDateTime.of(2026, 8, 6, 10, 0))
                .updatedAt(LocalDateTime.of(2026, 8, 6, 10, 0))
                .build();
    }

    private Project updateProject() {
        return Project.builder()
                .name("Updated Project")
                .description("Updated project description")
                .status(ProjectStatus.SUSPENDED)
                .environment(ProjectEnvironment.PRODUCTION)
                .tokenAudience("updated-project")
                .build();
    }

    private static class RecordingProjectPolicy extends ProjectPolicy {
        private Project checkedCreateProject;
        private Project checkedUpdateProject;

        private RecordingProjectPolicy() {
            super(new RecordingProjectRepository());
        }

        @Override
        public void ensureCanCreate(Project project) {
            checkedCreateProject = project;
        }

        @Override
        public void ensureCanUpdate(Project project) {
            checkedUpdateProject = project;
        }
    }

    private static class RecordingProjectRepository implements ProjectRepository {
        private final Optional<Project> projectById;
        private Project savedProject;
        private String deletedProjectId;

        private RecordingProjectRepository() {
            this(Optional.empty());
        }

        private RecordingProjectRepository(Optional<Project> projectById) {
            this.projectById = projectById;
        }

        @Override
        public Project save(Project project) {
            savedProject = project;

            return Project.builder()
                    .id(project.getId() == null ? "project-id" : project.getId())
                    .name(project.getName())
                    .description(project.getDescription())
                    .ownerAccountId(project.getOwnerAccountId())
                    .status(project.getStatus())
                    .environment(project.getEnvironment())
                    .tokenAudience(project.getTokenAudience())
                    .createdAt(project.getCreatedAt())
                    .updatedAt(project.getUpdatedAt())
                    .build();
        }

        @Override
        public Optional<Project> findById(String id) {
            return projectById;
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
            return null;
        }

        @Override
        public PageResult<Project> findAllByMemberAccountId(String accountId, Pagination pagination) {
            return new PageResult<>(List.of(), pagination.page(), pagination.size(), 0, 0);
        }

        @Override
        public void deleteById(String id) {
            deletedProjectId = id;
        }
    }

    private static class RecordingProjectMemberRepository implements ProjectMemberRepository {
        private boolean member;
        private boolean canManage;
        private ProjectMember savedProjectMember;
        private String readProjectId;
        private String readAccountId;
        private Collection<ProjectMemberRole> managementRoles;
        private String deletedProjectId;

        @Override
        public ProjectMember save(ProjectMember projectMember) {
            savedProjectMember = projectMember;
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
            deletedProjectId = projectId;
        }
    }

    private static class StubAccountRepository implements AccountRepository {
        private final Optional<Account> account;

        private StubAccountRepository(Optional<Account> account) {
            this.account = account;
        }

        @Override
        public Account save(Account account) {
            return account;
        }

        @Override
        public Optional<Account> findById(String id) {
            return account;
        }

        @Override
        public Optional<Account> findByEmail(String email) {
            return Optional.empty();
        }

        @Override
        public Optional<Account> findByCPF(CPF cpf) {
            return Optional.empty();
        }

        @Override
        public Optional<Account> findByEmailIgnoreCaseOrCPF(String email, CPF cpf) {
            return Optional.empty();
        }

        @Override
        public void deleteById(String id) {
        }
    }
}
