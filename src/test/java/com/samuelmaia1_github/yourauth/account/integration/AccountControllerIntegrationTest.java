package com.samuelmaia1_github.yourauth.account.integration;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:account_controller_integration_test",
        "spring.cache.type=simple",
        "spring.data.redis.host=localhost",
        "spring.data.redis.port=6379",
        "spring.data.redis.password="
})
@AutoConfigureMockMvc
class AccountControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TokenService tokenService;

    @Test
    void shouldFindAccountByEmailThroughHttp() throws Exception {
        insertAccount("requester-account", "requester@example.com", "12345678909", "Requester", "Account");
        insertAccount("target-account", "target@example.com", "98765432100", "Target", "Account");

        insertAccountSession("requester-session", "requester-account");
        String token = token("requester-account", "requester@example.com", "12345678909", "requester-session");

        mockMvc.perform(get("/accounts")
                        .header("Authorization", "Bearer " + token)
                        .param("email", "target@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("target-account"))
                .andExpect(jsonPath("$.name").value("Target"))
                .andExpect(jsonPath("$.lastName").value("Account"))
                .andExpect(jsonPath("$.email").value("target@example.com"))
                .andExpect(jsonPath("$.CPF").doesNotExist());
    }

    @Test
    void shouldRequireAuthenticationToFindAccountByEmail() throws Exception {
        mockMvc.perform(get("/accounts")
                        .param("email", "target@example.com"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Autenticação obrigatória."));
    }

    @Test
    void shouldKeepTraditionalAccountCreationFieldsRequired() throws Exception {
        mockMvc.perform(post("/accounts/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Traditional",
                                  "email": "traditional@example.com",
                                  "password": "Password1"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.lastName").exists())
                .andExpect(jsonPath("$.fields.CPF").exists())
                .andExpect(jsonPath("$.fields.address").exists())
                .andExpect(jsonPath("$.fields.phone").exists());
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
