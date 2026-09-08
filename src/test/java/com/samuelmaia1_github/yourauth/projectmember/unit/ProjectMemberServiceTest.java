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

    private static class RecordingProjectMemberDetailsRepository implements ProjectMemberDetailsRepository {
        private String projectId;
        private Pagination pagination;

        @Override
        public PageResult<ProjectMemberDetails> findAllByProjectId(String projectId, Pagination pagination) {
            this.projectId = projectId;
            this.pagination = pagination;

            return new PageResult<>(
                    List.of(new ProjectMemberDetails(
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
        private String projectId;
        private String accountId;

        private RecordingProjectMemberRepository(boolean member) {
            this.member = member;
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
    }
}
