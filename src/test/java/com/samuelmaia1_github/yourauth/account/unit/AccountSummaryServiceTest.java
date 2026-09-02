package com.samuelmaia1_github.yourauth.account.unit;

import com.samuelmaia1_github.yourauth.domain.account.AccountProjectSummary;
import com.samuelmaia1_github.yourauth.domain.account.AccountSummary;
import com.samuelmaia1_github.yourauth.domain.account.AccountSummaryRepository;
import com.samuelmaia1_github.yourauth.domain.account.AccountSummaryService;
import com.samuelmaia1_github.yourauth.domain.project.ProjectEnvironment;
import com.samuelmaia1_github.yourauth.domain.project.ProjectStatus;
import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMemberRole;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AccountSummaryServiceTest {
    @Test
    void shouldReturnSummaryForAccountProjects() {
        RecordingAccountSummaryRepository repository = new RecordingAccountSummaryRepository(List.of(
                projectSummary("project-1", ProjectMemberRole.OWNER, 10, 4),
                projectSummary("project-2", ProjectMemberRole.ADMIN, 5, 2)
        ));
        AccountSummaryService service = new AccountSummaryService(repository);

        AccountSummary summary = service.findByAccountId("account-id");

        assertThat(repository.searchedAccountId).isEqualTo("account-id");
        assertThat(summary.totalProjects()).isEqualTo(2);
        assertThat(summary.totalUsers()).isEqualTo(15);
        assertThat(summary.totalActiveSessions()).isEqualTo(6);
        assertThat(summary.projects()).extracting(AccountProjectSummary::role)
                .containsExactly(ProjectMemberRole.OWNER, ProjectMemberRole.ADMIN);
    }

    @Test
    void shouldReturnEmptySummaryWhenAccountHasNoProjects() {
        RecordingAccountSummaryRepository repository = new RecordingAccountSummaryRepository(List.of());
        AccountSummaryService service = new AccountSummaryService(repository);

        AccountSummary summary = service.findByAccountId("account-id");

        assertThat(summary.totalProjects()).isZero();
        assertThat(summary.totalUsers()).isZero();
        assertThat(summary.totalActiveSessions()).isZero();
        assertThat(summary.projects()).isEmpty();
    }

    private static AccountProjectSummary projectSummary(
            String id,
            ProjectMemberRole role,
            long totalUsers,
            long totalActiveSessions
    ) {
        return new AccountProjectSummary(
                id,
                "My Project",
                "Project description",
                "owner-account-id",
                ProjectStatus.ACTIVE,
                ProjectEnvironment.DEVELOPMENT,
                "my-project",
                LocalDateTime.of(2026, 8, 6, 10, 0),
                LocalDateTime.of(2026, 8, 6, 10, 0),
                role,
                totalUsers,
                totalActiveSessions
        );
    }

    private static class RecordingAccountSummaryRepository implements AccountSummaryRepository {
        private final List<AccountProjectSummary> projectSummaries;
        private String searchedAccountId;

        private RecordingAccountSummaryRepository(List<AccountProjectSummary> projectSummaries) {
            this.projectSummaries = projectSummaries;
        }

        @Override
        public List<AccountProjectSummary> findProjectSummariesByAccountId(String accountId) {
            searchedAccountId = accountId;
            return projectSummaries;
        }
    }
}
