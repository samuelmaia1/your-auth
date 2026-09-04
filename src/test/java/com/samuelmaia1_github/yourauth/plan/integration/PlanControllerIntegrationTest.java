package com.samuelmaia1_github.yourauth.plan.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:plan_controller_integration_test")
@AutoConfigureMockMvc
class PlanControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnActivePlansWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].code").value("FREE"))
                .andExpect(jsonPath("$[1].code").value("STARTER"))
                .andExpect(jsonPath("$[2].code").value("PRO"))
                .andExpect(jsonPath("$[3].code").value("BUSINESS"));
    }
}
