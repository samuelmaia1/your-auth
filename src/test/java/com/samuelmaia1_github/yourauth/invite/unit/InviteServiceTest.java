package com.samuelmaia1_github.yourauth.invite.unit;

import com.samuelmaia1_github.yourauth.domain.account.Account;
import com.samuelmaia1_github.yourauth.domain.account.AccountRepository;
import com.samuelmaia1_github.yourauth.domain.account.exceptions.AccountNotFoundException;
import com.samuelmaia1_github.yourauth.domain.invite.Invite;
import com.samuelmaia1_github.yourauth.domain.invite.InviteRepository;
import com.samuelmaia1_github.yourauth.domain.invite.InviteService;
import com.samuelmaia1_github.yourauth.domain.invite.InviteStatus;
import com.samuelmaia1_github.yourauth.domain.invite.exceptions.InviteAccessDeniedException;
import com.samuelmaia1_github.yourauth.domain.invite.exceptions.InviteConflictException;
import com.samuelmaia1_github.yourauth.domain.invite.exceptions.InviteInvalidException;
import com.samuelmaia1_github.yourauth.domain.project.Project;
import com.samuelmaia1_github.yourauth.domain.project.ProjectRepository;
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

class InviteServiceTest {
    private static final String PROJECT_ID = "project-id";
    private static final String SENDER_ACCOUNT_ID = "sender-account-id";
    private static final String SENDER_ACCOUNT_EMAIL = "sender@example.com";
    private static final String RECIPIENT_ACCOUNT_ID = "recipient-account-id";
    private static final String RECIPIENT_ACCOUNT_EMAIL = "recipient@example.com";

    @Test
    void shouldSendInviteWhenAuthenticatedAccountCanManageProject() {
        RecordingInviteRepository inviteRepository = new RecordingInviteRepository();
        RecordingProjectMemberRepository memberRepository = new RecordingProjectMemberRepository();
        memberRepository.managementMember = true;
        StubAccountRepository accountRepository = new StubAccountRepository(true);
        InviteService service = new InviteService(
                inviteRepository,
                new StubProjectRepository(),
                memberRepository,
                accountRepository
        );

        Invite invite = service.send(
                PROJECT_ID,
                SENDER_ACCOUNT_ID,
                RECIPIENT_ACCOUNT_ID,
                ProjectMemberRole.ADMIN
        );

        assertThat(invite.getSenderAccountId()).isEqualTo(SENDER_ACCOUNT_ID);
        assertThat(invite.getSenderAccountEmail()).isEqualTo(SENDER_ACCOUNT_EMAIL);
        assertThat(invite.getRecipientAccountId()).isEqualTo(RECIPIENT_ACCOUNT_ID);
        assertThat(invite.getRecipientAccountEmail()).isEqualTo(RECIPIENT_ACCOUNT_EMAIL);
        assertThat(invite.getRole()).isEqualTo(ProjectMemberRole.ADMIN);
        assertThat(invite.getStatus()).isEqualTo(InviteStatus.PENDING);
        assertThat(invite.getSentAt()).isNotNull();
        assertThat(invite.getProjectId()).isEqualTo(PROJECT_ID);
        assertThat(invite.getProjectName()).isEqualTo("Project Name");
        assertThat(invite.getProjectDescription()).isEqualTo("Project Description");
        assertThat(memberRepository.managementRoles).containsExactly(ProjectMemberRole.OWNER, ProjectMemberRole.ADMIN);
        assertThat(accountRepository.accountId).isEqualTo(RECIPIENT_ACCOUNT_ID);
        assertThat(inviteRepository.savedInvite).isSameAs(invite);
    }

    @Test
    void shouldDenySendInviteWhenAuthenticatedAccountCannotManageProject() {
        InviteService service = new InviteService(
                new RecordingInviteRepository(),
                new StubProjectRepository(),
                new RecordingProjectMemberRepository(),
                new StubAccountRepository(true)
        );

        assertThatThrownBy(() -> service.send(
                PROJECT_ID,
                SENDER_ACCOUNT_ID,
                RECIPIENT_ACCOUNT_ID,
                ProjectMemberRole.DEVELOPER
        ))
                .isInstanceOf(ProjectAccessDeniedException.class)
                .hasMessage("A conta autenticada não tem permissão para enviar convites neste projeto.");
    }

    @Test
    void shouldRejectOwnerRoleInvite() {
        RecordingProjectMemberRepository memberRepository = new RecordingProjectMemberRepository();
        memberRepository.managementMember = true;
        InviteService service = new InviteService(
                new RecordingInviteRepository(),
                new StubProjectRepository(),
                memberRepository,
                new StubAccountRepository(true)
        );

        assertThatThrownBy(() -> service.send(
                PROJECT_ID,
                SENDER_ACCOUNT_ID,
                RECIPIENT_ACCOUNT_ID,
                ProjectMemberRole.OWNER
        ))
                .isInstanceOf(InviteInvalidException.class)
                .hasMessage("Convites não podem atribuir a role OWNER.");
    }

    @Test
    void shouldRejectInviteWhenRecipientDoesNotExist() {
        RecordingProjectMemberRepository memberRepository = new RecordingProjectMemberRepository();
        memberRepository.managementMember = true;
        InviteService service = new InviteService(
                new RecordingInviteRepository(),
                new StubProjectRepository(),
                memberRepository,
                new StubAccountRepository(false)
        );

        assertThatThrownBy(() -> service.send(
                PROJECT_ID,
                SENDER_ACCOUNT_ID,
                RECIPIENT_ACCOUNT_ID,
                ProjectMemberRole.VIEWER
        ))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessage("Conta destinatária não encontrada.");
    }

    @Test
    void shouldRejectInviteWhenRecipientAlreadyIsProjectMember() {
        RecordingProjectMemberRepository memberRepository = new RecordingProjectMemberRepository();
        memberRepository.managementMember = true;
        memberRepository.projectMember = true;
        InviteService service = new InviteService(
                new RecordingInviteRepository(),
                new StubProjectRepository(),
                memberRepository,
                new StubAccountRepository(true)
        );

        assertThatThrownBy(() -> service.send(
                PROJECT_ID,
                SENDER_ACCOUNT_ID,
                RECIPIENT_ACCOUNT_ID,
                ProjectMemberRole.VIEWER
        ))
                .isInstanceOf(InviteConflictException.class)
                .hasMessage("A conta destinatária já participa deste projeto.");
    }

    @Test
    void shouldRejectInviteWhenPendingInviteAlreadyExists() {
        RecordingInviteRepository inviteRepository = new RecordingInviteRepository();
        inviteRepository.pendingInviteExists = true;
        RecordingProjectMemberRepository memberRepository = new RecordingProjectMemberRepository();
        memberRepository.managementMember = true;
        InviteService service = new InviteService(
                inviteRepository,
                new StubProjectRepository(),
                memberRepository,
                new StubAccountRepository(true)
        );

        assertThatThrownBy(() -> service.send(
                PROJECT_ID,
                SENDER_ACCOUNT_ID,
                RECIPIENT_ACCOUNT_ID,
                ProjectMemberRole.VIEWER
        ))
                .isInstanceOf(InviteConflictException.class)
                .hasMessage("Já existe um convite pendente para esta conta neste projeto.");
    }

    @Test
    void shouldListReceivedInvitesByAuthenticatedAccount() {
        RecordingInviteRepository inviteRepository = new RecordingInviteRepository();
        InviteService service = new InviteService(
                inviteRepository,
                new StubProjectRepository(),
                new RecordingProjectMemberRepository(),
                new StubAccountRepository(true)
        );
        Pagination pagination = new Pagination(1, 10);

        PageResult<Invite> invites = service.findAllReceived(RECIPIENT_ACCOUNT_ID, pagination, InviteStatus.PENDING);

        assertThat(invites.content()).hasSize(1);
        assertThat(invites.content().getFirst().getRecipientAccountId()).isEqualTo(RECIPIENT_ACCOUNT_ID);
        assertThat(inviteRepository.recipientAccountId).isEqualTo(RECIPIENT_ACCOUNT_ID);
        assertThat(inviteRepository.status).isEqualTo(InviteStatus.PENDING);
        assertThat(inviteRepository.pagination).isSameAs(pagination);
    }

    @Test
    void shouldListReceivedInvitesWithoutStatusFilter() {
        RecordingInviteRepository inviteRepository = new RecordingInviteRepository();
        InviteService service = new InviteService(
                inviteRepository,
                new StubProjectRepository(),
                new RecordingProjectMemberRepository(),
                new StubAccountRepository(true)
        );
        Pagination pagination = new Pagination(1, 10);

        PageResult<Invite> invites = service.findAllReceived(RECIPIENT_ACCOUNT_ID, pagination, null);

        assertThat(invites.content()).hasSize(1);
        assertThat(inviteRepository.recipientAccountId).isEqualTo(RECIPIENT_ACCOUNT_ID);
        assertThat(inviteRepository.status).isNull();
        assertThat(inviteRepository.pagination).isSameAs(pagination);
    }

    @Test
    void shouldListProjectInvitesWhenAuthenticatedAccountIsProjectMember() {
        RecordingInviteRepository inviteRepository = new RecordingInviteRepository();
        RecordingProjectMemberRepository memberRepository = new RecordingProjectMemberRepository();
        memberRepository.projectMember = true;
        InviteService service = new InviteService(
                inviteRepository,
                new StubProjectRepository(),
                memberRepository,
                new StubAccountRepository(true)
        );
        Pagination pagination = new Pagination(0, 20);

        PageResult<Invite> invites = service.findAllByProjectId(
                PROJECT_ID,
                SENDER_ACCOUNT_ID,
                pagination,
                InviteStatus.ACCEPTED
        );

        assertThat(invites.content()).hasSize(1);
        assertThat(inviteRepository.projectId).isEqualTo(PROJECT_ID);
        assertThat(inviteRepository.status).isEqualTo(InviteStatus.ACCEPTED);
        assertThat(inviteRepository.pagination).isSameAs(pagination);
    }

    @Test
    void shouldAcceptPendingInviteAndCreateProjectMember() {
        RecordingInviteRepository inviteRepository = new RecordingInviteRepository();
        inviteRepository.invite = pendingInvite(RECIPIENT_ACCOUNT_ID);
        RecordingProjectMemberRepository memberRepository = new RecordingProjectMemberRepository();
        InviteService service = new InviteService(
                inviteRepository,
                new StubProjectRepository(),
                memberRepository,
                new StubAccountRepository(true)
        );

        Invite invite = service.accept("invite-id", RECIPIENT_ACCOUNT_ID);

        assertThat(invite.getStatus()).isEqualTo(InviteStatus.ACCEPTED);
        assertThat(memberRepository.savedMember.getProjectId()).isEqualTo(PROJECT_ID);
        assertThat(memberRepository.savedMember.getAccountId()).isEqualTo(RECIPIENT_ACCOUNT_ID);
        assertThat(memberRepository.savedMember.getRole()).isEqualTo(ProjectMemberRole.DEVELOPER);
        assertThat(inviteRepository.savedInvite.getStatus()).isEqualTo(InviteStatus.ACCEPTED);
    }

    @Test
    void shouldRefusePendingInvite() {
        RecordingInviteRepository inviteRepository = new RecordingInviteRepository();
        inviteRepository.invite = pendingInvite(RECIPIENT_ACCOUNT_ID);
        RecordingProjectMemberRepository memberRepository = new RecordingProjectMemberRepository();
        InviteService service = new InviteService(
                inviteRepository,
                new StubProjectRepository(),
                memberRepository,
                new StubAccountRepository(true)
        );

        Invite invite = service.refuse("invite-id", RECIPIENT_ACCOUNT_ID);

        assertThat(invite.getStatus()).isEqualTo(InviteStatus.REFUSED);
        assertThat(inviteRepository.savedInvite.getStatus()).isEqualTo(InviteStatus.REFUSED);
        assertThat(memberRepository.savedMember).isNull();
    }

    @Test
    void shouldDenyAcceptWhenInviteDoesNotBelongToAuthenticatedAccount() {
        RecordingInviteRepository inviteRepository = new RecordingInviteRepository();
        inviteRepository.invite = pendingInvite("another-account-id");
        InviteService service = new InviteService(
                inviteRepository,
                new StubProjectRepository(),
                new RecordingProjectMemberRepository(),
                new StubAccountRepository(true)
        );

        assertThatThrownBy(() -> service.accept("invite-id", RECIPIENT_ACCOUNT_ID))
                .isInstanceOf(InviteAccessDeniedException.class)
                .hasMessage("A conta autenticada não tem permissão para acessar este convite.");
    }

    @Test
    void shouldRejectAcceptWhenInviteIsNotPending() {
        RecordingInviteRepository inviteRepository = new RecordingInviteRepository();
        inviteRepository.invite = Invite.builder()
                .id("invite-id")
                .senderAccountId(SENDER_ACCOUNT_ID)
                .senderAccountEmail(SENDER_ACCOUNT_EMAIL)
                .recipientAccountId(RECIPIENT_ACCOUNT_ID)
                .recipientAccountEmail(RECIPIENT_ACCOUNT_EMAIL)
                .role(ProjectMemberRole.DEVELOPER)
                .status(InviteStatus.ACCEPTED)
                .projectId(PROJECT_ID)
                .build();
        InviteService service = new InviteService(
                inviteRepository,
                new StubProjectRepository(),
                new RecordingProjectMemberRepository(),
                new StubAccountRepository(true)
        );

        assertThatThrownBy(() -> service.accept("invite-id", RECIPIENT_ACCOUNT_ID))
                .isInstanceOf(InviteConflictException.class)
                .hasMessage("Apenas convites pendentes podem ser aceitos.");
    }

    @Test
    void shouldRejectRefuseWhenInviteIsNotPending() {
        RecordingInviteRepository inviteRepository = new RecordingInviteRepository();
        inviteRepository.invite = Invite.builder()
                .id("invite-id")
                .senderAccountId(SENDER_ACCOUNT_ID)
                .senderAccountEmail(SENDER_ACCOUNT_EMAIL)
                .recipientAccountId(RECIPIENT_ACCOUNT_ID)
                .recipientAccountEmail(RECIPIENT_ACCOUNT_EMAIL)
                .role(ProjectMemberRole.DEVELOPER)
                .status(InviteStatus.ACCEPTED)
                .projectId(PROJECT_ID)
                .build();
        InviteService service = new InviteService(
                inviteRepository,
                new StubProjectRepository(),
                new RecordingProjectMemberRepository(),
                new StubAccountRepository(true)
        );

        assertThatThrownBy(() -> service.refuse("invite-id", RECIPIENT_ACCOUNT_ID))
                .isInstanceOf(InviteConflictException.class)
                .hasMessage("Apenas convites pendentes podem ser recusados.");
    }

    private static Invite pendingInvite(String recipientAccountId) {
        return Invite.builder()
                .id("invite-id")
                .senderAccountId(SENDER_ACCOUNT_ID)
                .senderAccountEmail(SENDER_ACCOUNT_EMAIL)
                .recipientAccountId(recipientAccountId)
                .recipientAccountEmail(RECIPIENT_ACCOUNT_EMAIL)
                .role(ProjectMemberRole.DEVELOPER)
                .status(InviteStatus.PENDING)
                .sentAt(LocalDateTime.parse("2026-01-01T10:00:00"))
                .projectId(PROJECT_ID)
                .projectName("Project Name")
                .projectDescription("Project Description")
                .build();
    }

    private static class RecordingInviteRepository implements InviteRepository {
        private boolean pendingInviteExists;
        private Invite invite;
        private Invite savedInvite;
        private String recipientAccountId;
        private String projectId;
        private Pagination pagination;
        private InviteStatus status;

        @Override
        public Invite save(Invite invite) {
            this.savedInvite = invite;
            return invite;
        }

        @Override
        public Optional<Invite> findById(String id) {
            return Optional.ofNullable(invite);
        }

        @Override
        public PageResult<Invite> findAllByRecipientAccountId(
                String recipientAccountId,
                Pagination pagination,
                InviteStatus status
        ) {
            this.recipientAccountId = recipientAccountId;
            this.pagination = pagination;
            this.status = status;

            return new PageResult<>(
                    List.of(pendingInvite(recipientAccountId)),
                    pagination.page(),
                    pagination.size(),
                    1,
                    1
            );
        }

        @Override
        public PageResult<Invite> findAllByProjectId(String projectId, Pagination pagination, InviteStatus status) {
            this.projectId = projectId;
            this.pagination = pagination;
            this.status = status;

            return new PageResult<>(
                    List.of(pendingInvite(RECIPIENT_ACCOUNT_ID)),
                    pagination.page(),
                    pagination.size(),
                    1,
                    1
            );
        }

        @Override
        public boolean existsByProjectIdAndRecipientAccountIdAndStatus(
                String projectId,
                String recipientAccountId,
                InviteStatus status
        ) {
            return pendingInviteExists;
        }
    }

    private static class StubProjectRepository implements ProjectRepository {
        @Override
        public Project save(Project project) {
            return project;
        }

        @Override
        public Optional<Project> findById(String id) {
            return Optional.of(Project.builder()
                    .id(id)
                    .name("Project Name")
                    .description("Project Description")
                    .build());
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
        private boolean managementMember;
        private boolean projectMember;
        private ProjectMember savedMember;
        private Collection<ProjectMemberRole> managementRoles;

        @Override
        public ProjectMember save(ProjectMember projectMember) {
            this.savedMember = projectMember;
            return projectMember;
        }

        @Override
        public Optional<ProjectMember> findByProjectIdAndAccountId(String projectId, String accountId) {
            return Optional.empty();
        }

        @Override
        public boolean existsByProjectIdAndAccountId(String projectId, String accountId) {
            return projectMember;
        }

        @Override
        public boolean existsByProjectIdAndAccountIdAndRoleIn(
                String projectId,
                String accountId,
                Collection<ProjectMemberRole> roles
        ) {
            this.managementRoles = roles;
            return managementMember;
        }

        @Override
        public void deleteAllByProjectId(String projectId) {
        }

        @Override
        public void deleteByProjectIdAndAccountId(String projectId, String accountId) {
        }
    }

    private static class StubAccountRepository implements AccountRepository {
        private final boolean recipientExists;
        private String accountId;

        private StubAccountRepository(boolean recipientExists) {
            this.recipientExists = recipientExists;
        }

        @Override
        public Account save(Account account) {
            return account;
        }

        @Override
        public Optional<Account> findById(String id) {
            this.accountId = id;

            if (RECIPIENT_ACCOUNT_ID.equals(id) && !recipientExists) {
                return Optional.empty();
            }

            return Optional.of(Account.builder()
                    .id(id)
                    .email(SENDER_ACCOUNT_ID.equals(id) ? SENDER_ACCOUNT_EMAIL : RECIPIENT_ACCOUNT_EMAIL)
                    .CPF(new CPF("12345678909"))
                    .build());
        }

        @Override
        public Optional<Account> findByEmail(String email) {
            return Optional.empty();
        }

        @Override
        public Optional<Account> findByEmailIgnoreCase(String email) {
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
