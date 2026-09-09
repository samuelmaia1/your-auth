package com.samuelmaia1_github.yourauth.projectmember.unit;

import com.samuelmaia1_github.yourauth.domain.project.Project;
import com.samuelmaia1_github.yourauth.domain.project.ProjectRepository;
import com.samuelmaia1_github.yourauth.domain.project.exceptions.ProjectAccessDeniedException;
import com.samuelmaia1_github.yourauth.domain.project.exceptions.ProjectNotFoundException;
import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMember;
import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMemberDetails;
import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMemberDetailsRepository;
import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMemberRepository;
import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMemberRole;
import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMemberService;
import com.samuelmaia1_github.yourauth.domain.projectmember.exceptions.ProjectMemberNotFoundException;
import com.samuelmaia1_github.yourauth.domain.shared.PageResult;
import com.samuelmaia1_github.yourauth.domain.shared.Pagination;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProjectMemberServiceTest {
    private static final String PROJECT_ID = "project-id";
    private static final String ACCOUNT_ID = "account-id";

    @Test
    void shouldListMembersWhenAuthenticatedAccountIsProjectMember() {
        RecordingProjectMemberDetailsRepository detailsRepository = new RecordingProjectMemberDetailsRepository();
        RecordingProjectMemberRepository memberRepository = new RecordingProjectMemberRepository(true);
        ProjectMemberService service = new ProjectMemberService(
                detailsRepository,
                new StubProjectRepository(true),
                memberRepository
        );
        Pagination pagination = new Pagination(1, 10);

        PageResult<ProjectMemberDetails> members = service.findAllByProjectId(PROJECT_ID, ACCOUNT_ID, pagination);

        assertThat(members.content()).hasSize(1);
        assertThat(members.content().getFirst().accountId()).isEqualTo(ACCOUNT_ID);
        assertThat(members.content().getFirst().name()).isEqualTo("Samuel");
        assertThat(members.content().getFirst().lastName()).isEqualTo("Maia");
        assertThat(members.content().getFirst().role()).isEqualTo(ProjectMemberRole.OWNER);
        assertThat(members.page()).isEqualTo(1);
        assertThat(members.size()).isEqualTo(10);
        assertThat(memberRepository.projectId).isEqualTo(PROJECT_ID);
        assertThat(memberRepository.accountId).isEqualTo(ACCOUNT_ID);
        assertThat(detailsRepository.projectId).isEqualTo(PROJECT_ID);
        assertThat(detailsRepository.pagination).isSameAs(pagination);
    }

    @Test
    void shouldDenyListMembersWhenAuthenticatedAccountIsNotProjectMember() {
        RecordingProjectMemberDetailsRepository detailsRepository = new RecordingProjectMemberDetailsRepository();
        ProjectMemberService service = new ProjectMemberService(
                detailsRepository,
                new StubProjectRepository(true),
                new RecordingProjectMemberRepository(false)
        );

        assertThatThrownBy(() -> service.findAllByProjectId(PROJECT_ID, ACCOUNT_ID, new Pagination(0, 20)))
                .isInstanceOf(ProjectAccessDeniedException.class)
                .hasMessage("A conta autenticada não tem permissão para acessar este projeto.");

        assertThat(detailsRepository.projectId).isNull();
    }

    @Test
    void shouldThrowWhenProjectDoesNotExist() {
        RecordingProjectMemberDetailsRepository detailsRepository = new RecordingProjectMemberDetailsRepository();
        RecordingProjectMemberRepository memberRepository = new RecordingProjectMemberRepository(true);
        ProjectMemberService service = new ProjectMemberService(
                detailsRepository,
                new StubProjectRepository(false),
                memberRepository
        );

        assertThatThrownBy(() -> service.findAllByProjectId(PROJECT_ID, ACCOUNT_ID, new Pagination(0, 20)))
                .isInstanceOf(ProjectNotFoundException.class)
                .hasMessage("Projeto não encontrado: " + PROJECT_ID);

        assertThat(memberRepository.projectId).isNull();
        assertThat(detailsRepository.projectId).isNull();
    }

    @Test
    void shouldDeleteDeveloperWhenAuthenticatedAccountIsAdmin() {
        RecordingProjectMemberRepository memberRepository = new RecordingProjectMemberRepository(true);
        memberRepository.requester = member("admin-account-id", ProjectMemberRole.ADMIN);
        memberRepository.target = member("developer-account-id", ProjectMemberRole.DEVELOPER);
        ProjectMemberService service = new ProjectMemberService(
                new RecordingProjectMemberDetailsRepository(),
                new StubProjectRepository(true),
                memberRepository
        );

        service.delete(PROJECT_ID, "developer-account-id", "admin-account-id");

        assertThat(memberRepository.deletedProjectId).isEqualTo(PROJECT_ID);
        assertThat(memberRepository.deletedAccountId).isEqualTo("developer-account-id");
    }

    @Test
    void shouldDeleteAdminWhenAuthenticatedAccountIsOwner() {
        RecordingProjectMemberRepository memberRepository = new RecordingProjectMemberRepository(true);
        memberRepository.requester = member("owner-account-id", ProjectMemberRole.OWNER);
        memberRepository.target = member("admin-account-id", ProjectMemberRole.ADMIN);
        ProjectMemberService service = new ProjectMemberService(
                new RecordingProjectMemberDetailsRepository(),
                new StubProjectRepository(true),
                memberRepository
        );

        service.delete(PROJECT_ID, "admin-account-id", "owner-account-id");

        assertThat(memberRepository.deletedProjectId).isEqualTo(PROJECT_ID);
        assertThat(memberRepository.deletedAccountId).isEqualTo("admin-account-id");
    }

    @Test
    void shouldDenyDeleteAdminWhenAuthenticatedAccountIsAdmin() {
        RecordingProjectMemberRepository memberRepository = new RecordingProjectMemberRepository(true);
        memberRepository.requester = member("admin-account-id", ProjectMemberRole.ADMIN);
        memberRepository.target = member("another-admin-account-id", ProjectMemberRole.ADMIN);
        ProjectMemberService service = new ProjectMemberService(
                new RecordingProjectMemberDetailsRepository(),
                new StubProjectRepository(true),
                memberRepository
        );

        assertThatThrownBy(() -> service.delete(PROJECT_ID, "another-admin-account-id", "admin-account-id"))
                .isInstanceOf(ProjectAccessDeniedException.class)
                .hasMessage("Apenas o owner pode deletar um membro admin.");

        assertThat(memberRepository.deletedAccountId).isNull();
    }

    @Test
    void shouldDenyDeleteWhenAuthenticatedAccountCannotManageMembers() {
        RecordingProjectMemberRepository memberRepository = new RecordingProjectMemberRepository(true);
        memberRepository.requester = member("viewer-account-id", ProjectMemberRole.VIEWER);
        memberRepository.target = member("developer-account-id", ProjectMemberRole.DEVELOPER);
        ProjectMemberService service = new ProjectMemberService(
                new RecordingProjectMemberDetailsRepository(),
                new StubProjectRepository(true),
                memberRepository
        );

        assertThatThrownBy(() -> service.delete(PROJECT_ID, "developer-account-id", "viewer-account-id"))
                .isInstanceOf(ProjectAccessDeniedException.class);

        assertThat(memberRepository.deletedAccountId).isNull();
    }

    @Test
    void shouldDenyDeleteOwnerMember() {
        RecordingProjectMemberRepository memberRepository = new RecordingProjectMemberRepository(true);
        memberRepository.requester = member("owner-account-id", ProjectMemberRole.OWNER);
        memberRepository.target = member("owner-account-id", ProjectMemberRole.OWNER);
        ProjectMemberService service = new ProjectMemberService(
                new RecordingProjectMemberDetailsRepository(),
                new StubProjectRepository(true),
                memberRepository
        );

        assertThatThrownBy(() -> service.delete(PROJECT_ID, "owner-account-id", "owner-account-id"))
                .isInstanceOf(ProjectAccessDeniedException.class)
                .hasMessage("O owner do projeto não pode ser removido dos membros.");

        assertThat(memberRepository.deletedAccountId).isNull();
    }

    @Test
    void shouldThrowWhenTargetMemberDoesNotExist() {
        RecordingProjectMemberRepository memberRepository = new RecordingProjectMemberRepository(true);
        memberRepository.requester = member("owner-account-id", ProjectMemberRole.OWNER);
        ProjectMemberService service = new ProjectMemberService(
                new RecordingProjectMemberDetailsRepository(),
                new StubProjectRepository(true),
                memberRepository
        );

        assertThatThrownBy(() -> service.delete(PROJECT_ID, "missing-account-id", "owner-account-id"))
                .isInstanceOf(ProjectMemberNotFoundException.class)
                .hasMessage("Membro do projeto não encontrado.");

        assertThat(memberRepository.deletedAccountId).isNull();
    }

    private static class RecordingProjectMemberDetailsRepository implements ProjectMemberDetailsRepository {
        private String projectId;
        private Pagination pagination;

        @Override
        public PageResult<ProjectMemberDetails> findAllByProjectId(String projectId, Pagination pagination) {
            this.projectId = projectId;
            this.pagination = pagination;

            return new PageResult<>(
                    List.of(new ProjectMemberDetails(
                            ACCOUNT_ID,
                            "Samuel",
                            "Maia",
                            ProjectMemberRole.OWNER,
                            LocalDateTime.parse("2026-01-01T10:00:00")
                    )),
                    pagination.page(),
                    pagination.size(),
                    1,
                    1
            );
        }
    }

    private static ProjectMember member(String accountId, ProjectMemberRole role) {
        return ProjectMember.builder()
                .projectId(PROJECT_ID)
                .accountId(accountId)
                .role(role)
                .build();
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
        private ProjectMember requester;
        private ProjectMember target;
        private String projectId;
        private String accountId;
        private String deletedProjectId;
        private String deletedAccountId;

        private RecordingProjectMemberRepository(boolean member) {
            this.member = member;
        }

        @Override
        public ProjectMember save(ProjectMember projectMember) {
            return projectMember;
        }

        @Override
        public Optional<ProjectMember> findByProjectIdAndAccountId(String projectId, String accountId) {
            if (requester != null && accountId.equals(requester.getAccountId())) {
                return Optional.of(requester);
            }

            if (target != null && accountId.equals(target.getAccountId())) {
                return Optional.of(target);
            }

            return Optional.empty();
        }

        @Override
        public boolean existsByProjectIdAndAccountId(String projectId, String accountId) {
            this.projectId = projectId;
            this.accountId = accountId;
            return member;
        }

        @Override
        public boolean existsByProjectIdAndAccountIdAndRoleIn(
                String projectId,
                String accountId,
                Collection<ProjectMemberRole> roles
        ) {
            return false;
        }

        @Override
        public void deleteAllByProjectId(String projectId) {
        }

        @Override
        public void deleteByProjectIdAndAccountId(String projectId, String accountId) {
            deletedProjectId = projectId;
            deletedAccountId = accountId;
        }
    }
}
