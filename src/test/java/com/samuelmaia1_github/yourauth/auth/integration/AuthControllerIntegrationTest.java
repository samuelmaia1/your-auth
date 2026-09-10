package com.samuelmaia1_github.yourauth.auth.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.samuelmaia1_github.yourauth.infra.interfaces.IPasswordEncoder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:auth_controller_integration_test",
        "spring.cache.type=simple",
        "spring.data.redis.host=localhost",
        "spring.data.redis.port=6379",
        "spring.data.redis.password="
})
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private IPasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldInvalidateAccountAccessTokenWhenSessionIsRevokedThroughLogout() throws Exception {
        insertAccount("account-id", "account-auth@example.com", "12345678909", "raw-password");

        MvcResult loginResult = mockMvc.perform(post("/auth/mobile/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("User-Agent", "test-agent")
                        .header("X-End-User-IP", "203.0.113.10")
                        .header("X-Device-Name", "Test Device")
                        .content("""
                                {
                                  "email": "account-auth@example.com",
                                  "password": "raw-password"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode loginBody = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String accessToken = loginBody.get("accessToken").asText();
        String refreshToken = loginBody.get("refreshToken").asText();

        assertThat(activeSessions("account-id")).isEqualTo(1);

        mockMvc.perform(get("/accounts")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("email", "account-auth@example.com"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/auth/mobile/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "%s"
                                }
                                """.formatted(refreshToken)))
                .andExpect(status().isNoContent());

        assertThat(activeSessions("account-id")).isZero();

        mockMvc.perform(get("/accounts")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("email", "account-auth@example.com"))
                .andExpect(status().isUnauthorized());
    }

    private Integer activeSessions(String accountId) {
        return jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM account_sessions
                        WHERE account_id = ?
                          AND revoked_at IS NULL
                        """,
                Integer.class,
                accountId
        );
    }

    private void insertAccount(String id, String email, String cpf, String password) {
        jdbcTemplate.update("""
                INSERT INTO accounts (
                    id,
                    email,
                    password,
                    cpf,
                    name,
                    last_name,
                    phone_ddd,
                    phone_number,
                    address_postal_code,
                    address_street,
                    address_number,
                    address_neighborhood,
                    address_city,
                    address_state
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                id,
                email,
                passwordEncoder.encode(password),
                cpf,
                "Auth",
                "Account",
                "11",
                "999999999",
                "01001000",
                "Street",
                "100",
                "Center",
                "Sao Paulo",
                "SP"
        );
    }
}
