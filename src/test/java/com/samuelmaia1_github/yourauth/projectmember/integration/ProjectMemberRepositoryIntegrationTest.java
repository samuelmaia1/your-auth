package com.samuelmaia1_github.yourauth.projectmember.integration;

import com.samuelmaia1_github.yourauth.domain.account.Account;
import com.samuelmaia1_github.yourauth.domain.auth.TokenService;
import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMemberDetails;
import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMemberDetailsRepository;
import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMemberRole;
import com.samuelmaia1_github.yourauth.domain.shared.PageResult;
import com.samuelmaia1_github.yourauth.domain.shared.Pagination;
import com.samuelmaia1_github.yourauth.domain.valueobjects.CPF;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:project_member_repository_test",
        "spring.cache.type=simple",
        "spring.data.redis.host=localhost",
        "spring.data.redis.port=6379",
        "spring.data.redis.password="
})
@AutoConfigureMockMvc
class ProjectMemberRepositoryIntegrationTest {
    @Autowired
    private ProjectMemberDetailsRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TokenService tokenService;

    @Test
    void shouldListProjectMembersWithAccountNames() {
        insertAccount("owner-account", "owner@example.com", "12345678909", "Ana", "Owner");
        insertAccount("admin-account", "admin@example.com", "98765432100", "Bruno", "Admin");
        insertAccount("viewer-account", "viewer@example.com", "11122233344", "Carla", "Viewer");
        insertProject("project-id", "owner-account");
        insertProjectMember(
                "owner-member",
                "project-id",
                "owner-account",
                "OWNER",
                LocalDateTime.parse("2026-01-01T10:00:00")
        );
        insertProjectMember(
                "admin-member",
                "project-id",
                "admin-account",
                "ADMIN",
                LocalDateTime.parse("2026-01-03T10:00:00")
        );
        insertProjectMember(
                "viewer-member",
                "project-id",
                "viewer-account",
                "VIEWER",
                LocalDateTime.parse("2026-01-02T10:00:00")
        );

        PageResult<ProjectMemberDetails> members = repository.findAllByProjectId(
                "project-id",
                new Pagination(0, 2)
        );

        assertThat(members.page()).isZero();
        assertThat(members.size()).isEqualTo(2);
        assertThat(members.totalElements()).isEqualTo(3);
        assertThat(members.totalPages()).isEqualTo(2);
        assertThat(members.content())
                .extracting(ProjectMemberDetails::accountId)
                .containsExactly("admin-account", "viewer-account");
        assertThat(members.content())
                .extracting(ProjectMemberDetails::name)
                .containsExactly("Bruno", "Carla");
        assertThat(members.content())
                .extracting(ProjectMemberDetails::lastName)
                .containsExactly("Admin", "Viewer");
        assertThat(members.content())
                .extracting(ProjectMemberDetails::role)
                .containsExactly(ProjectMemberRole.ADMIN, ProjectMemberRole.VIEWER);
        assertThat(members.content())
                .extracting(ProjectMemberDetails::joinedAt)
                .containsExactly(
                        LocalDateTime.parse("2026-01-03T10:00:00"),
                        LocalDateTime.parse("2026-01-02T10:00:00")
                );
    }

    @Test
    void shouldListProjectMembersThroughHttp() throws Exception {
        insertAccount("http-owner", "http-owner@example.com", "22233344455", "Http", "Owner");
        insertAccount("http-admin", "http-admin@example.com", "33344455566", "Http", "Admin");
        insertProject("http-project", "http-owner");
        insertProjectMember(
                "http-owner-member",
                "http-project",
                "http-owner",
                "OWNER",
                LocalDateTime.parse("2026-02-01T10:00:00")
        );
        insertProjectMember(
                "http-admin-member",
                "http-project",
                "http-admin",
                "ADMIN",
                LocalDateTime.parse("2026-02-02T10:00:00")
        );
        insertAccountSession("http-owner-session", "http-owner");

        String token = tokenService.generateToken(Account.builder()
                .id("http-owner")
                .email("http-owner@example.com")
                .CPF(new CPF("22233344455"))
                .build(), "http-owner-session");

        mockMvc.perform(get("/projects/http-project/members")
                        .header("Authorization", "Bearer " + token)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].accountId").value("http-admin"))
                .andExpect(jsonPath("$.content[0].name").value("Http"))
                .andExpect(jsonPath("$.content[0].lastName").value("Admin"))
                .andExpect(jsonPath("$.content[0].role").value("ADMIN"))
                .andExpect(jsonPath("$.content[0].joinedAt").value("2026-02-02T10:00:00"))
                .andExpect(jsonPath("$.content[1].accountId").value("http-owner"))
                .andExpect(jsonPath("$.content[1].name").value("Http"))
                .andExpect(jsonPath("$.content[1].lastName").value("Owner"))
                .andExpect(jsonPath("$.content[1].role").value("OWNER"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    private void insertAccount(String id, String email, String cpf, String name, String lastName) {
        jdbcTemplate.update("""
                INSERT INTO accounts (
                    id,
                    email,
                    password,
                    cpf,
                    name,
                    last_name
                ) VALUES (?, ?, ?, ?, ?, ?)
                """, id, email, "hashed-password", cpf, name, lastName);
    }

    private void insertAccountSession(String id, String accountId) {
        jdbcTemplate.update("""
                INSERT INTO account_sessions (
                    id,
                    account_id,
                    last_used_at
                ) VALUES (?, ?, CURRENT_TIMESTAMP)
                """, id, accountId);
    }

    private void insertProject(String id, String ownerAccountId) {
        jdbcTemplate.update("""
                INSERT INTO projects (
                    id,
                    name,
                    owner_account_id,
                    status,
                    environment,
                    token_audience
                ) VALUES (?, ?, ?, ?, ?, ?)
                """, id, "Project", ownerAccountId, "ACTIVE", "DEVELOPMENT", "test-audience");
    }

    private void insertProjectMember(
            String id,
            String projectId,
            String accountId,
            String role,
            LocalDateTime joinedAt
    ) {
        jdbcTemplate.update("""
                INSERT INTO project_members (
                    id,
                    project_id,
                    account_id,
                    role,
                    joined_at
                ) VALUES (?, ?, ?, ?, ?)
                """, id, projectId, accountId, role, joinedAt);
    }
}
