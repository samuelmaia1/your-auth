package com.samuelmaia1_github.yourauth.user.unit;

import com.samuelmaia1_github.yourauth.domain.project.Project;
import com.samuelmaia1_github.yourauth.domain.project.ProjectRepository;
import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMember;
import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMemberRepository;
import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMemberRole;
import com.samuelmaia1_github.yourauth.domain.shared.PageResult;
import com.samuelmaia1_github.yourauth.domain.shared.Pagination;
import com.samuelmaia1_github.yourauth.domain.shared.Phone;
import com.samuelmaia1_github.yourauth.domain.user.User;
import com.samuelmaia1_github.yourauth.domain.user.UserFilter;
import com.samuelmaia1_github.yourauth.domain.user.UserPolicy;
import com.samuelmaia1_github.yourauth.domain.user.UserRepository;
import com.samuelmaia1_github.yourauth.domain.user.UserService;
import com.samuelmaia1_github.yourauth.domain.user.UserStatus;
import com.samuelmaia1_github.yourauth.domain.user.UserUpdate;
import com.samuelmaia1_github.yourauth.infra.interfaces.IPasswordEncoder;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class UserServiceTest {
    private static final String PROJECT_ID = "project-id";
    private static final String USER_ID = "user-id";
    private static final String ACCOUNT_ID = "account-id";

    @Test
    void shouldUpdateOnlyProvidedUserFields() {
        RecordingUserRepository userRepository = new RecordingUserRepository(Optional.of(currentUser()));
        RecordingPasswordEncoder encoder = new RecordingPasswordEncoder();
        UserService service = new UserService(
                userRepository,
                new StubProjectRepository(true),
                new RecordingProjectMemberRepository(true),
                new UserPolicy(userRepository),
                encoder
        );

        User updatedUser = service.update(
                PROJECT_ID,
                USER_ID,
                new UserUpdate(
                        null,
                        false,
                        "new-password",
                        true,
                        null,
                        true
                ),
                ACCOUNT_ID
        );

        assertThat(updatedUser.getEmail()).isEqualTo("user@example.com");
        assertThat(updatedUser.getPassword()).isEqualTo("encoded:new-password");
        assertThat(updatedUser.getPhone()).isNull();
        assertThat(updatedUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(updatedUser.getLastLoginAt()).isEqualTo(LocalDateTime.of(2026, 8, 6, 10, 0));
        assertThat(userRepository.savedUser).isSameAs(updatedUser);
        assertThat(encoder.rawPassword).isEqualTo("new-password");
    }

    private static User currentUser() {
        return User.builder()
                .id(USER_ID)
                .projectId(PROJECT_ID)
                .email("user@example.com")
                .password("encoded:old-password")
                .status(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.of(2026, 8, 1, 10, 0))
                .updatedAt(LocalDateTime.of(2026, 8, 2, 10, 0))
                .lastLoginAt(LocalDateTime.of(2026, 8, 6, 10, 0))
                .lastPasswordChangedAt(LocalDateTime.of(2026, 8, 1, 10, 0))
                .failedLoginAttempts(2)
                .phone(Phone.builder()
                        .ddd("11")
                        .number("999999999")
                        .build())
                .build();
    }

    private static class RecordingUserRepository implements UserRepository {
        private final Optional<User> currentUser;
        private User savedUser;

        private RecordingUserRepository(Optional<User> currentUser) {
            this.currentUser = currentUser;
        }

        @Override
        public User save(User user) {
            savedUser = user;
            return user;
        }

        @Override
        public Optional<User> findById(String id) {
            return currentUser;
        }

        @Override
        public Optional<User> findByProjectIdAndId(String projectId, String id) {
            return currentUser;
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

    private static class RecordingPasswordEncoder implements IPasswordEncoder {
        private String rawPassword;

        @Override
        public String encode(String raw) {
            rawPassword = raw;
            return "encoded:" + raw;
        }

        @Override
        public Boolean matches(String raw, String hash) {
            return false;
        }
    }
}
