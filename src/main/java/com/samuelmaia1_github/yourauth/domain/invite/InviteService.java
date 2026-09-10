package com.samuelmaia1_github.yourauth.domain.invite;

import com.samuelmaia1_github.yourauth.domain.account.Account;
import com.samuelmaia1_github.yourauth.domain.account.AccountRepository;
import com.samuelmaia1_github.yourauth.domain.account.exceptions.AccountNotFoundException;
import com.samuelmaia1_github.yourauth.domain.invite.exceptions.InviteAccessDeniedException;
import com.samuelmaia1_github.yourauth.domain.invite.exceptions.InviteConflictException;
import com.samuelmaia1_github.yourauth.domain.invite.exceptions.InviteInvalidException;
import com.samuelmaia1_github.yourauth.domain.invite.exceptions.InviteNotFoundException;
import com.samuelmaia1_github.yourauth.domain.project.Project;
import com.samuelmaia1_github.yourauth.domain.project.ProjectRepository;
import com.samuelmaia1_github.yourauth.domain.project.exceptions.ProjectAccessDeniedException;
import com.samuelmaia1_github.yourauth.domain.project.exceptions.ProjectNotFoundException;
import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMember;
import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMemberRepository;
import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMemberRole;
import com.samuelmaia1_github.yourauth.domain.shared.PageResult;
import com.samuelmaia1_github.yourauth.domain.shared.Pagination;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.samuelmaia1_github.yourauth.infra.cache.names.AccountCacheNames.ACCOUNT_SUMMARY_BY_ACCOUNT_ID;
import static com.samuelmaia1_github.yourauth.infra.cache.names.ProjectCacheNames.PROJECT_BY_ACCOUNT_ID;
import static com.samuelmaia1_github.yourauth.infra.cache.names.ProjectCacheNames.PROJECT_BY_ID;
import static com.samuelmaia1_github.yourauth.infra.cache.names.ProjectMemberCacheNames.PROJECT_MEMBERS_BY_PROJECT_ID;

@Service
@RequiredArgsConstructor
public class InviteService {
    private static final List<ProjectMemberRole> INVITE_MANAGEMENT_ROLES = List.of(
            ProjectMemberRole.OWNER,
            ProjectMemberRole.ADMIN
    );

    private final InviteRepository inviteRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public Invite send(String projectId, String senderAccountId, String recipientAccountId, ProjectMemberRole role) {
        Project project = findProjectOrThrow(projectId);

        ensureCanSendInvite(project.getId(), senderAccountId);
        ensureValidInvite(senderAccountId, recipientAccountId, role);
        Account senderAccount = findSenderAccountOrThrow(senderAccountId);
        Account recipientAccount = findRecipientAccountOrThrow(recipientAccountId);
        ensureRecipientIsNotMember(project.getId(), recipientAccountId);
        ensureNoPendingInvite(project.getId(), recipientAccountId);

        Invite invite = Invite.builder()
                .senderAccountId(senderAccountId)
                .senderAccountEmail(senderAccount.getEmail())
                .recipientAccountId(recipientAccountId)
                .recipientAccountEmail(recipientAccount.getEmail())
                .role(role)
                .status(InviteStatus.PENDING)
                .sentAt(LocalDateTime.now())
                .projectId(project.getId())
                .projectName(project.getName())
                .projectDescription(project.getDescription())
                .build();

        return inviteRepository.save(invite);
    }

    public PageResult<Invite> findAllReceived(
            String recipientAccountId,
            Pagination pagination,
            InviteStatus status
    ) {
        return inviteRepository.findAllByRecipientAccountId(recipientAccountId, pagination, status);
    }

    public PageResult<Invite> findAllByProjectId(
            String projectId,
            String authenticatedAccountId,
            Pagination pagination,
            InviteStatus status
    ) {
        ensureProjectExists(projectId);
        ensureCanRead(projectId, authenticatedAccountId);

        return inviteRepository.findAllByProjectId(projectId, pagination, status);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(
                    cacheNames = PROJECT_MEMBERS_BY_PROJECT_ID,
                    allEntries = true
            ),
            @CacheEvict(
                    cacheNames = PROJECT_BY_ID,
                    allEntries = true
            ),
            @CacheEvict(
                    cacheNames = PROJECT_BY_ACCOUNT_ID,
                    allEntries = true
            ),
            @CacheEvict(
                    cacheNames = ACCOUNT_SUMMARY_BY_ACCOUNT_ID,
                    allEntries = true
            )
    })
    public Invite accept(String inviteId, String authenticatedAccountId) {
        Invite invite = findInviteOrThrow(inviteId);

        ensureCanAnswerInvite(invite, authenticatedAccountId);
        ensurePending(invite, "aceitos");
        ensureRecipientIsNotMember(invite.getProjectId(), authenticatedAccountId);

        projectMemberRepository.save(ProjectMember.builder()
                .projectId(invite.getProjectId())
                .accountId(authenticatedAccountId)
                .role(invite.getRole())
                .build());

        invite.accept();

        return inviteRepository.save(invite);
    }

    @Transactional
    public Invite refuse(String inviteId, String authenticatedAccountId) {
        Invite invite = findInviteOrThrow(inviteId);

        ensureCanAnswerInvite(invite, authenticatedAccountId);
        ensurePending(invite, "recusados");

        invite.refuse();

        return inviteRepository.save(invite);
    }

    private Project findProjectOrThrow(String projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(ProjectNotFoundException::new);
    }

    private void ensureProjectExists(String projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ProjectNotFoundException("Projeto não encontrado: " + projectId);
        }
    }

    private Invite findInviteOrThrow(String inviteId) {
        return inviteRepository.findById(inviteId)
                .orElseThrow(InviteNotFoundException::new);
    }

    private void ensureCanSendInvite(String projectId, String senderAccountId) {
        if (!projectMemberRepository.existsByProjectIdAndAccountIdAndRoleIn(
                projectId,
                senderAccountId,
                INVITE_MANAGEMENT_ROLES
        )) {
            throw new ProjectAccessDeniedException("A conta autenticada não tem permissão para enviar convites neste projeto.");
        }
    }

    private void ensureValidInvite(String senderAccountId, String recipientAccountId, ProjectMemberRole role) {
        if (senderAccountId.equals(recipientAccountId)) {
            throw new InviteInvalidException("A conta não pode convidar a si mesma para o projeto.");
        }

        if (role == null) {
            throw new InviteInvalidException("Role do convite é obrigatória.");
        }

        if (ProjectMemberRole.OWNER.equals(role)) {
            throw new InviteInvalidException("Convites não podem atribuir a role OWNER.");
        }
    }

    private Account findSenderAccountOrThrow(String senderAccountId) {
        return accountRepository.findById(senderAccountId)
                .orElseThrow(() -> new AccountNotFoundException("Conta remetente não encontrada."));
    }

    private Account findRecipientAccountOrThrow(String recipientAccountId) {
        return accountRepository.findById(recipientAccountId)
                .orElseThrow(() -> new AccountNotFoundException("Conta destinatária não encontrada."));
    }

    private void ensureRecipientIsNotMember(String projectId, String recipientAccountId) {
        if (projectMemberRepository.existsByProjectIdAndAccountId(projectId, recipientAccountId)) {
            throw new InviteConflictException("A conta destinatária já participa deste projeto.");
        }
    }

    private void ensureNoPendingInvite(String projectId, String recipientAccountId) {
        if (inviteRepository.existsByProjectIdAndRecipientAccountIdAndStatus(
                projectId,
                recipientAccountId,
                InviteStatus.PENDING
        )) {
            throw new InviteConflictException("Já existe um convite pendente para esta conta neste projeto.");
        }
    }

    private void ensureCanAnswerInvite(Invite invite, String authenticatedAccountId) {
        if (!authenticatedAccountId.equals(invite.getRecipientAccountId())) {
            throw new InviteAccessDeniedException();
        }
    }

    private void ensureCanRead(String projectId, String accountId) {
        if (!projectMemberRepository.existsByProjectIdAndAccountId(projectId, accountId)) {
            throw new ProjectAccessDeniedException();
        }
    }

    private void ensurePending(Invite invite, String action) {
        if (!InviteStatus.PENDING.equals(invite.getStatus())) {
            throw new InviteConflictException("Apenas convites pendentes podem ser " + action + ".");
        }
    }
}
