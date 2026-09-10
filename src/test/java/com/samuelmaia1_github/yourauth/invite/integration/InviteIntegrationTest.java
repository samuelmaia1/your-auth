package com.samuelmaia1_github.yourauth.invite.integration;

import com.samuelmaia1_github.yourauth.domain.account.Account;
import com.samuelmaia1_github.yourauth.domain.auth.TokenService;
import com.samuelmaia1_github.yourauth.domain.valueobjects.CPF;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:invite_integration_test",
        "spring.cache.type=simple",
        "spring.data.redis.host=localhost",
        "spring.data.redis.port=6379",
        "spring.data.redis.password="
})
@AutoConfigureMockMvc
class InviteIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TokenService tokenService;

    @Test
    void shouldSendListAcceptAndRefuseInviteThroughHttp() throws Exception {
        insertAccount("owner-account", "owner-invite@example.com", "12345678909", "Owner", "Account");
        insertAccount("recipient-account", "recipient-invite@example.com", "98765432100", "Recipient", "Account");
        insertAccount("refuse-recipient-account", "refuse-recipient-invite@example.com", "11122233344", "Refuse", "Account");
        insertProject("invite-project", "owner-account");
        insertProjectMember("owner-member", "invite-project", "owner-account", "OWNER");

        insertAccountSession("owner-session", "owner-account");
        insertAccountSession("recipient-session", "recipient-account");
        insertAccountSession("refuse-recipient-session", "refuse-recipient-account");
        String ownerToken = token("owner-account", "owner-invite@example.com", "12345678909", "owner-session");
        String recipientToken = token(
                "recipient-account",
                "recipient-invite@example.com",
                "98765432100",
                "recipient-session"
        );
        String refuseRecipientToken = token(
                "refuse-recipient-account",
                "refuse-recipient-invite@example.com",
                "11122233344",
                "refuse-recipient-session"
        );

        mockMvc.perform(post("/projects/invite-project/invites")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "recipientAccountId": "recipient-account",
                                  "role": "DEVELOPER"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.senderAccountId").value("owner-account"))
                .andExpect(jsonPath("$.senderAccountEmail").value("owner-invite@example.com"))
                .andExpect(jsonPath("$.recipientAccountId").value("recipient-account"))
                .andExpect(jsonPath("$.recipientAccountEmail").value("recipient-invite@example.com"))
                .andExpect(jsonPath("$.role").value("DEVELOPER"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.projectId").value("invite-project"))
                .andExpect(jsonPath("$.projectName").value("Invite Project"))
                .andExpect(jsonPath("$.projectDescription").value("Invite Project Description"))
                .andExpect(jsonPath("$.sentAt").exists());

        String inviteId = jdbcTemplate.queryForObject(
                "SELECT id FROM invites WHERE recipient_account_id = ?",
                String.class,
                "recipient-account"
        );

        mockMvc.perform(get("/invites/received")
                        .header("Authorization", "Bearer " + recipientToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(inviteId))
                .andExpect(jsonPath("$.content[0].recipientAccountId").value("recipient-account"))
                .andExpect(jsonPath("$.content[0].recipientAccountEmail").value("recipient-invite@example.com"))
                .andExpect(jsonPath("$.content[0].status").value("PENDING"));

        mockMvc.perform(get("/invites/received")
                        .header("Authorization", "Bearer " + recipientToken)
                        .param("page", "0")
                        .param("size", "10")
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(inviteId))
                .andExpect(jsonPath("$.content[0].status").value("PENDING"));

        mockMvc.perform(get("/projects/invite-project/invites")
                        .header("Authorization", "Bearer " + ownerToken)
                        .param("page", "0")
                        .param("size", "10")
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(inviteId))
                .andExpect(jsonPath("$.content[0].senderAccountEmail").value("owner-invite@example.com"))
                .andExpect(jsonPath("$.content[0].status").value("PENDING"));

        mockMvc.perform(post("/invites/{inviteId}/accept", inviteId)
                        .header("Authorization", "Bearer " + recipientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(inviteId))
                .andExpect(jsonPath("$.status").value("ACCEPTED"));

        mockMvc.perform(get("/invites/received")
                        .header("Authorization", "Bearer " + recipientToken)
                        .param("page", "0")
                        .param("size", "10")
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));

        Integer memberCount = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM project_members
                        WHERE project_id = ?
                            AND account_id = ?
                            AND role = ?
                        """,
                Integer.class,
                "invite-project",
                "recipient-account",
                "DEVELOPER"
        );

        assertThat(memberCount).isEqualTo(1);

        mockMvc.perform(post("/projects/invite-project/invites")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "recipientAccountId": "refuse-recipient-account",
                                  "role": "VIEWER"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.recipientAccountEmail").value("refuse-recipient-invite@example.com"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        String refusedInviteId = jdbcTemplate.queryForObject(
                "SELECT id FROM invites WHERE recipient_account_id = ?",
                String.class,
                "refuse-recipient-account"
        );

        mockMvc.perform(post("/invites/{inviteId}/refuse", refusedInviteId)
                        .header("Authorization", "Bearer " + refuseRecipientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(refusedInviteId))
                .andExpect(jsonPath("$.status").value("REFUSED"));

        Integer refusedInviteCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM invites WHERE id = ? AND status = ?",
                Integer.class,
                refusedInviteId,
                "REFUSED"
        );

        assertThat(refusedInviteCount).isEqualTo(1);

        mockMvc.perform(get("/projects/invite-project/invites")
                        .header("Authorization", "Bearer " + ownerToken)
                        .param("page", "0")
                        .param("size", "10")
                        .param("status", "REFUSED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(refusedInviteId))
                .andExpect(jsonPath("$.content[0].status").value("REFUSED"));
    }

    private String token(String id, String email, String cpf, String sessionId) {
        return tokenService.generateToken(Account.builder()
                .id(id)
                .email(email)
                .CPF(new CPF(cpf))
                .build(), sessionId);
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

    private void insertProject(String id, String ownerAccountId) {
        jdbcTemplate.update("""
                INSERT INTO projects (
                    id,
                    name,
                    description,
                    owner_account_id,
                    status,
                    environment,
                    token_audience
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
                """, id, "Invite Project", "Invite Project Description", ownerAccountId, "ACTIVE", "DEVELOPMENT", "test-audience");
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

    private void insertAccountSession(String id, String accountId) {
        jdbcTemplate.update("""
                INSERT INTO account_sessions (
                    id,
                    account_id,
                    last_used_at
                ) VALUES (?, ?, CURRENT_TIMESTAMP)
                """, id, accountId);
    }
}
