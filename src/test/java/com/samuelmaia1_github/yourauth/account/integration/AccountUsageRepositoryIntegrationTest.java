package com.samuelmaia1_github.yourauth.account.integration;

import com.samuelmaia1_github.yourauth.domain.account.AccountUsage;
import com.samuelmaia1_github.yourauth.domain.account.AccountUsageRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:account_usage_repository_test")
class AccountUsageRepositoryIntegrationTest {
    @Autowired
    private AccountUsageRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldCountOnlyProjectsOwnedByAccount() {
        insertAccount("account-owner", "owner@example.com", "12345678909");
        insertAccount("account-admin", "admin@example.com", "98765432100");

        insertProject("owned-project-1", "account-owner", "Owned 1");
        insertProject("owned-project-2", "account-owner", "Owned 2");
        insertProject("member-project", "account-admin", "Member Project");
        insertProjectMember("member-link", "member-project", "account-owner", "ADMIN");

        insertUser("owned-user-1", "owned-project-1", "owned1@example.com");
        insertUser("owned-user-2", "owned-project-2", "owned2@example.com");
        insertUser("member-user", "member-project", "member@example.com");

        insertSession("owned-session-1", "owned-project-1", "owned-user-1", false);
        insertSession("owned-session-2", "owned-project-2", "owned-user-2", false);
        insertSession("revoked-owned-session", "owned-project-2", "owned-user-2", true);
        insertSession("member-session", "member-project", "member-user", false);

        AccountUsage usage = repository.findByOwnerAccountId("account-owner");

        assertThat(usage.totalProjects()).isEqualTo(2L);
        assertThat(usage.totalUsers()).isEqualTo(2L);
        assertThat(usage.totalActiveSessions()).isEqualTo(2L);
    }

    private void insertAccount(String id, String email, String cpf) {
        jdbcTemplate.update("""
                INSERT INTO accounts (
                    id,
                    email,
                    password,
                    cpf,
                    name,
                    last_name
                ) VALUES (?, ?, ?, ?, ?, ?)
                """, id, email, "hashed-password", cpf, "Test", "Account");
    }

    private void insertProject(String id, String ownerAccountId, String name) {
        jdbcTemplate.update("""
                INSERT INTO projects (
                    id,
                    name,
                    owner_account_id,
                    status,
                    environment,
                    token_audience
                ) VALUES (?, ?, ?, ?, ?, ?)
                """, id, name, ownerAccountId, "ACTIVE", "DEVELOPMENT", "test-audience");
    }

    private void insertProjectMember(String id, String projectId, String accountId, String role) {
        jdbcTemplate.update("""
                INSERT INTO project_members (
                    id,
                    project_id,
                    account_id,
                    role
                ) VALUES (?, ?, ?, ?)
                """, id, projectId, accountId, role);
    }

    private void insertUser(String id, String projectId, String email) {
        jdbcTemplate.update("""
                INSERT INTO users (
                    id,
                    project_id,
                    email,
                    password,
                    status
                ) VALUES (?, ?, ?, ?, ?)
                """, id, projectId, email, "hashed-password", "ACTIVE");
    }

    private void insertSession(String id, String projectId, String userId, boolean revoked) {
        jdbcTemplate.update("""
                INSERT INTO user_sessions (
                    id,
                    project_id,
                    user_id,
                    revoked_at
                ) VALUES (?, ?, ?, CASE WHEN ? THEN CURRENT_TIMESTAMP ELSE NULL END)
                """, id, projectId, userId, revoked);
    }
}
