package com.samuelmaia1_github.yourauth.user.integration;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:user_controller_security_test")
@AutoConfigureMockMvc
class UserControllerSecurityTest {
    private static final String REFRESH_TOKEN = "refresh-token";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRequireApiKeyToRefreshUserSession() throws Exception {
        mockMvc.perform(post("/users/refresh")
                        .cookie(new Cookie("refresh_token", REFRESH_TOKEN)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Autenticação obrigatória."));
    }

    @Test
    void shouldRequireApiKeyToLogoutUserSession() throws Exception {
        mockMvc.perform(post("/users/logout")
                        .cookie(new Cookie("refresh_token", REFRESH_TOKEN)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Autenticação obrigatória."));
    }
}
