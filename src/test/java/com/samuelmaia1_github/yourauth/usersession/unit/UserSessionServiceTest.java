package com.samuelmaia1_github.yourauth.usersession.unit;

import com.samuelmaia1_github.yourauth.domain.project.Project;
import com.samuelmaia1_github.yourauth.domain.project.ProjectRepository;
import com.samuelmaia1_github.yourauth.domain.project.exceptions.ProjectAccessDeniedException;
import com.samuelmaia1_github.yourauth.domain.project.exceptions.ProjectNotFoundException;
import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMember;
import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMemberRepository;
import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMemberRole;
import com.samuelmaia1_github.yourauth.domain.refreshtoken.UserRefreshToken;
import com.samuelmaia1_github.yourauth.domain.refreshtoken.UserRefreshTokenRepository;
import com.samuelmaia1_github.yourauth.domain.shared.PageResult;
import com.samuelmaia1_github.yourauth.domain.shared.Pagination;
import com.samuelmaia1_github.yourauth.domain.user.User;
import com.samuelmaia1_github.yourauth.domain.user.UserFilter;
import com.samuelmaia1_github.yourauth.domain.user.UserRepository;
import com.samuelmaia1_github.yourauth.domain.usersession.UserSession;
import com.samuelmaia1_github.yourauth.domain.usersession.UserSessionDetails;
import com.samuelmaia1_github.yourauth.domain.usersession.UserSessionDetailsRepository;
import com.samuelmaia1_github.yourauth.domain.usersession.UserSessionFilter;
import com.samuelmaia1_github.yourauth.domain.usersession.UserSessionRepository;
import com.samuelmaia1_github.yourauth.domain.usersession.UserSessionService;
import com.samuelmaia1_github.yourauth.domain.usersession.UserSessionStatus;
import com.samuelmaia1_github.yourauth.domain.usersession.exceptions.UserSessionNotFoundException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserSessionServiceTest {
    private static final String PROJECT_ID = "project-id";
    private static final String ACCOUNT_ID = "account-id";
    private static final UserSessionFilter EMPTY_FILTER = new UserSessionFilter(null, null, null, null);

    @Test
    void shouldListSessionsWhenAuthenticatedAccountIsProjectMember() {
        RecordingUserSessionDetailsRepository sessionsRepository = new RecordingUserSessionDetailsRepository();
        RecordingProjectMemberRepository memberRepository = new RecordingProjectMemberRepository(true);
        UserSessionService service = new UserSessionService(
                sessionsRepository,
                new RecordingUserSessionRepository(activeSession()),
                new RecordingUserRefreshTokenRepository(),
                new StubUserRepository(true),
                new StubProjectRepository(true),
                memberRepository
        );
        Pagination pagination = new Pagination(1, 10);

        PageResult<UserSessionDetails> sessions = service.findAllByProjectId(
                PROJECT_ID,
                ACCOUNT_ID,
                pagination,
                EMPTY_FILTER
        );

        assertThat(sessions.content()).hasSize(1);
        assertThat(sessions.page()).isEqualTo(1);
        assertThat(sessions.size()).isEqualTo(10);
        assertThat(sessions.totalElements()).isEqualTo(1);
        assertThat(sessionsRepository.projectId).isEqualTo(PROJECT_ID);
        assertThat(sessionsRepository.pagination).isSameAs(pagination);
        assertThat(sessionsRepository.filter).isSameAs(EMPTY_FILTER);
        assertThat(memberRepository.projectId).isEqualTo(PROJECT_ID);
        assertThat(memberRepository.accountId).isEqualTo(ACCOUNT_ID);
    }

    @Test
    void shouldListSessionsWithFilters() {
        RecordingUserSessionDetailsRepository sessionsRepository = new RecordingUserSessionDetailsRepository();
        UserSessionService service = new UserSessionService(
                sessionsRepository,
                new RecordingUserSessionRepository(activeSession()),
                new RecordingUserRefreshTokenRepository(),
                new StubUserRepository(true),
                new StubProjectRepository(true),
                new RecordingProjectMemberRepository(true)
        );
        Pagination pagination = new Pagination(0, 20);
        UserSessionFilter filter = new UserSessionFilter(
                UserSessionStatus.ACTIVE,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-31T23:59:59Z"),
                " user@email.com "
        );

        service.findAllByProjectId(PROJECT_ID, ACCOUNT_ID, pagination, filter);

        assertThat(sessionsRepository.pagination).isSameAs(pagination);
        assertThat(sessionsRepository.filter).isSameAs(filter);
        assertThat(sessionsRepository.filter.userEmail()).isEqualTo("user@email.com");
    }

    @Test
    void shouldRejectInvalidLastUsedAtRange() {
        assertThatThrownBy(() -> new UserSessionFilter(
                null,
                Instant.parse("2026-02-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:00Z"),
                null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A data inicial do último uso não pode ser posterior à data final.");
    }

    @Test
    void shouldDenyListSessionsWhenAuthenticatedAccountIsNotProjectMember() {
        RecordingUserSessionDetailsRepository sessionsRepository = new RecordingUserSessionDetailsRepository();
        UserSessionService service = new UserSessionService(
                sessionsRepository,
                new RecordingUserSessionRepository(activeSession()),
                new RecordingUserRefreshTokenRepository(),
                new StubUserRepository(true),
                new StubProjectRepository(true),
                new RecordingProjectMemberRepository(false)
        );

        assertThatThrownBy(() -> service.findAllByProjectId(PROJECT_ID, ACCOUNT_ID, new Pagination(0, 20), EMPTY_FILTER))
                .isInstanceOf(ProjectAccessDeniedException.class)
                .hasMessage("A conta autenticada não tem permissão para acessar este projeto.");

        assertThat(sessionsRepository.projectId).isNull();
    }

    @Test
    void shouldThrowWhenProjectDoesNotExist() {
        RecordingUserSessionDetailsRepository sessionsRepository = new RecordingUserSessionDetailsRepository();
        RecordingProjectMemberRepository memberRepository = new RecordingProjectMemberRepository(true);
        UserSessionService service = new UserSessionService(
                sessionsRepository,
                new RecordingUserSessionRepository(activeSession()),
                new RecordingUserRefreshTokenRepository(),
                new StubUserRepository(true),
                new StubProjectRepository(false),
                memberRepository
        );

        assertThatThrownBy(() -> service.findAllByProjectId(PROJECT_ID, ACCOUNT_ID, new Pagination(0, 20), EMPTY_FILTER))
                .isInstanceOf(ProjectNotFoundException.class)
                .hasMessage("Projeto não encontrado: " + PROJECT_ID);

        assertThat(memberRepository.projectId).isNull();
        assertThat(sessionsRepository.projectId).isNull();
    }

    @Test
    void shouldRevokeSessionAndRefreshTokensWhenAuthenticatedAccountCanManageSessions() {
        RecordingUserSessionRepository sessionRepository = new RecordingUserSessionRepository(activeSession());
        RecordingUserRefreshTokenRepository refreshTokenRepository = new RecordingUserRefreshTokenRepository();
        RecordingProjectMemberRepository memberRepository = new RecordingProjectMemberRepository(true, true);
        UserSessionService service = new UserSessionService(
                new RecordingUserSessionDetailsRepository(),
                sessionRepository,
                refreshTokenRepository,
                new StubUserRepository(true),
                new StubProjectRepository(true),
                memberRepository
        );

        service.revokeById(PROJECT_ID, "user-id", "session-id", ACCOUNT_ID);

        assertThat(refreshTokenRepository.revokedSessionId).isEqualTo("session-id");
        assertThat(sessionRepository.revokedSessionId).isEqualTo("session-id");
        assertThat(memberRepository.managementRoles).containsExactly(ProjectMemberRole.OWNER, ProjectMemberRole.ADMIN);
    }

    @Test
    void shouldRevokeAllUserSessionsAndRefreshTokensWhenAuthenticatedAccountCanManageSessions() {
        RecordingUserSessionRepository sessionRepository = new RecordingUserSessionRepository(activeSession());
        RecordingUserRefreshTokenRepository refreshTokenRepository = new RecordingUserRefreshTokenRepository();
        UserSessionService service = new UserSessionService(
                new RecordingUserSessionDetailsRepository(),
                sessionRepository,
                refreshTokenRepository,
                new StubUserRepository(true),
                new StubProjectRepository(true),
                new RecordingProjectMemberRepository(true, true)
        );

        service.revokeAllByUserId(PROJECT_ID, "user-id", ACCOUNT_ID);

        assertThat(refreshTokenRepository.revokedAllProjectId).isEqualTo(PROJECT_ID);
        assertThat(refreshTokenRepository.revokedAllUserId).isEqualTo("user-id");
        assertThat(sessionRepository.revokedAllProjectId).isEqualTo(PROJECT_ID);
        assertThat(sessionRepository.revokedAllUserId).isEqualTo("user-id");
    }

    @Test
    void shouldDenyRevokeSessionWhenAuthenticatedAccountCannotManageSessions() {
        RecordingUserSessionRepository sessionRepository = new RecordingUserSessionRepository(activeSession());
        RecordingUserRefreshTokenRepository refreshTokenRepository = new RecordingUserRefreshTokenRepository();
        UserSessionService service = new UserSessionService(
                new RecordingUserSessionDetailsRepository(),
                sessionRepository,
                refreshTokenRepository,
                new StubUserRepository(true),
                new StubProjectRepository(true),
                new RecordingProjectMemberRepository(true, false)
        );

        assertThatThrownBy(() -> service.revokeById(PROJECT_ID, "user-id", "session-id", ACCOUNT_ID))
                .isInstanceOf(ProjectAccessDeniedException.class);

        assertThat(refreshTokenRepository.revokedSessionId).isNull();
        assertThat(sessionRepository.revokedSessionId).isNull();
    }

    @Test
    void shouldRejectRevokingSessionThatDoesNotBelongToUser() {
        RecordingUserSessionRepository sessionRepository = new RecordingUserSessionRepository(
                UserSession.builder()
                        .id("session-id")
                        .projectId(PROJECT_ID)
                        .userId("another-user-id")
                        .build()
        );
        RecordingUserRefreshTokenRepository refreshTokenRepository = new RecordingUserRefreshTokenRepository();
        UserSessionService service = new UserSessionService(
                new RecordingUserSessionDetailsRepository(),
                sessionRepository,
                refreshTokenRepository,
                new StubUserRepository(true),
                new StubProjectRepository(true),
                new RecordingProjectMemberRepository(true, true)
        );

        assertThatThrownBy(() -> service.revokeById(PROJECT_ID, "user-id", "session-id", ACCOUNT_ID))
                .isInstanceOf(UserSessionNotFoundException.class)
                .hasMessage("Sessão de usuário não encontrada.");

        assertThat(refreshTokenRepository.revokedSessionId).isNull();
        assertThat(sessionRepository.revokedSessionId).isNull();
    }

    private static class RecordingUserSessionDetailsRepository implements UserSessionDetailsRepository {
        private String projectId;
        private Pagination pagination;
        private UserSessionFilter filter;

        @Override
        public PageResult<UserSessionDetails> findAllByProjectId(
                String projectId,
                Pagination pagination,
                UserSessionFilter filter
        ) {
            this.projectId = projectId;
            this.pagination = pagination;
            this.filter = filter;

            return new PageResult<>(
                    List.of(new UserSessionDetails(
                            UserSession.builder()
                                    .id("session-id")
                                    .projectId(projectId)
                                    .userId("user-id")
                                    .build(),
                            User.builder()
                                    .id("user-id")
                                    .projectId(projectId)
                                    .email("user@email.com")
                                    .build()
                    )),
                    pagination.page(),
                    pagination.size(),
                    1,
                    1
            );
        }
    }

    private static UserSession activeSession() {
        return UserSession.builder()
                .id("session-id")
                .projectId(PROJECT_ID)
                .userId("user-id")
                .lastUsedAt(Instant.now())
                .build();
    }

    private static class RecordingUserSessionRepository implements UserSessionRepository {
        private final UserSession session;
        private String revokedSessionId;
        private String revokedAllProjectId;
        private String revokedAllUserId;

        private RecordingUserSessionRepository(UserSession session) {
            this.session = session;
        }

        @Override
        public UserSession save(UserSession userSession) {
            return userSession;
        }

        @Override
        public Optional<UserSession> findById(String id) {
            if (session != null && id.equals(session.getId())) {
                return Optional.of(session);
            }

            return Optional.empty();
        }

        @Override
        public List<UserSession> findAllByProjectIdAndUserId(String projectId, String userId) {
            return List.of();
        }

        @Override
        public List<UserSession> findAllByProjectIdAndUserIdAndRevokedAtIsNull(String projectId, String userId) {
            return List.of();
        }

        @Override
        public void revokeById(String id) {
            revokedSessionId = id;
        }

        @Override
        public void revokeAllByProjectIdAndUserId(String projectId, String userId) {
            revokedAllProjectId = projectId;
            revokedAllUserId = userId;
        }

        @Override
        public long countByProjectIdAndUserIdAndRevokedAtIsNull(String projectId, String userId) {
            return 0;
        }
    }

    private static class RecordingUserRefreshTokenRepository implements UserRefreshTokenRepository {
        private String revokedSessionId;
        private String revokedAllProjectId;
        private String revokedAllUserId;

        @Override
        public UserRefreshToken save(UserRefreshToken refreshToken) {
            return refreshToken;
        }

        @Override
        public Optional<UserRefreshToken> findById(String id) {
            return Optional.empty();
        }

        @Override
        public Optional<UserRefreshToken> findByHash(String hash) {
            return Optional.empty();
        }

        @Override
        public List<UserRefreshToken> findAllByProjectIdAndUserId(String projectId, String userId) {
            return List.of();
        }

        @Override
        public List<UserRefreshToken> findAllBySessionId(String sessionId) {
            return List.of();
        }

        @Override
        public void revokeSession(String sessionId) {
            revokedSessionId = sessionId;
        }

        @Override
        public void revokeAllByProjectIdAndUserId(String projectId, String userId) {
            revokedAllProjectId = projectId;
            revokedAllUserId = userId;
        }

        @Override
        public void deleteById(String id) {
        }
    }

    private static class StubUserRepository implements UserRepository {
        private final boolean exists;

        private StubUserRepository(boolean exists) {
            this.exists = exists;
        }

        @Override
        public User save(User user) {
            return user;
        }

        @Override
        public Optional<User> findById(String id) {
            return exists ? Optional.of(User.builder().id(id).build()) : Optional.empty();
        }

        @Override
        public Optional<User> findByProjectIdAndId(String projectId, String id) {
            return exists ? Optional.of(User.builder().id(id).projectId(projectId).build()) : Optional.empty();
        }

        @Override
        public Optional<User> findByProjectIdAndEmailIgnoreCase(String projectId, String email) {
            return Optional.empty();
        }

        @Override
        public boolean existsByProjectIdAndEmailIgnoreCase(String projectId, String email) {
            return false;
        }

        @Override
        public boolean existsByProjectIdAndEmailIgnoreCaseAndIdNot(String projectId, String email, String id) {
            return false;
        }

        @Override
        public PageResult<User> findAllByProjectId(String projectId, Pagination pagination, UserFilter filter) {
            return new PageResult<>(List.of(), pagination.page(), pagination.size(), 0, 0);
        }

        @Override
        public void deleteById(String id) {
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
        private String projectId;
        private String accountId;
        private Collection<ProjectMemberRole> managementRoles;

        private RecordingProjectMemberRepository(boolean member) {
            this(member, false);
        }

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
            this.projectId = projectId;
            this.accountId = accountId;
            this.managementRoles = roles;
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
