package com.samuelmaia1_github.yourauth.domain.usersession;

import com.samuelmaia1_github.yourauth.domain.project.ProjectRepository;
import com.samuelmaia1_github.yourauth.domain.project.exceptions.ProjectAccessDeniedException;
import com.samuelmaia1_github.yourauth.domain.project.exceptions.ProjectNotFoundException;
import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMemberRepository;
import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMemberRole;
import com.samuelmaia1_github.yourauth.domain.refreshtoken.UserRefreshTokenRepository;
import com.samuelmaia1_github.yourauth.domain.shared.PageResult;
import com.samuelmaia1_github.yourauth.domain.shared.Pagination;
import com.samuelmaia1_github.yourauth.domain.user.UserRepository;
import com.samuelmaia1_github.yourauth.domain.user.exceptions.UserNotFoundException;
import com.samuelmaia1_github.yourauth.domain.usersession.exceptions.UserSessionNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserSessionService {
    private static final List<ProjectMemberRole> USER_SESSION_MANAGEMENT_ROLES = List.of(
            ProjectMemberRole.OWNER,
            ProjectMemberRole.ADMIN
    );

    private final UserSessionDetailsRepository detailsRepository;
    private final UserSessionRepository sessionRepository;
    private final UserRefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public PageResult<UserSessionDetails> findAllByProjectId(
            String projectId,
            String accountId,
            Pagination pagination,
            UserSessionFilter filter
    ) {
        ensureProjectExists(projectId);
        ensureCanRead(projectId, accountId);

        return detailsRepository.findAllByProjectId(projectId, pagination, filter);
    }

    @Transactional
    public void revokeById(String projectId, String userId, String sessionId, String accountId) {
        ensureProjectExists(projectId);
        ensureCanManage(projectId, accountId);
        ensureUserExists(projectId, userId);

        UserSession session = findSessionOrThrow(sessionId);
        ensureSessionBelongsToUser(projectId, userId, session);

        refreshTokenRepository.revokeSession(session.getId());
        sessionRepository.revokeById(session.getId());
    }

    @Transactional
    public void revokeAllByUserId(String projectId, String userId, String accountId) {
        ensureProjectExists(projectId);
        ensureCanManage(projectId, accountId);
        ensureUserExists(projectId, userId);

        refreshTokenRepository.revokeAllByProjectIdAndUserId(projectId, userId);
        sessionRepository.revokeAllByProjectIdAndUserId(projectId, userId);
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
                USER_SESSION_MANAGEMENT_ROLES
        )) {
            throw new ProjectAccessDeniedException();
        }
    }

    private void ensureUserExists(String projectId, String userId) {
        if (userRepository.findByProjectIdAndId(projectId, userId).isEmpty()) {
            throw new UserNotFoundException();
        }
    }

    private UserSession findSessionOrThrow(String sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(UserSessionNotFoundException::new);
    }

    private void ensureSessionBelongsToUser(String projectId, String userId, UserSession session) {
        if (!projectId.equals(session.getProjectId()) || !userId.equals(session.getUserId())) {
            throw new UserSessionNotFoundException();
        }
    }
}
