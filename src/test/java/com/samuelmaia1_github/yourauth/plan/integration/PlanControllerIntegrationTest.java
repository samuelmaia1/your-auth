package com.samuelmaia1_github.yourauth.plan.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:plan_controller_integration_test",
        "spring.cache.type=simple",
        "spring.data.redis.host=localhost",
        "spring.data.redis.port=6379",
        "spring.data.redis.password="
})
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
                .andExpect(jsonPath("$[3].code").value("BUSINESS"))
                .andExpect(jsonPath("$[0].limits.length()").value(3));
    }

    @Test
    void shouldUpdatePlanLimitsWithoutAuthentication() throws Exception {
        mockMvc.perform(put("/plans/FREE/limits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "MAX_PROJECTS": 2,
                                  "MAX_USERS_TOTAL": 250,
                                  "MAX_ACTIVE_SESSIONS_TOTAL": 500
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("FREE"))
                .andExpect(jsonPath("$.limits[?(@.code == 'MAX_PROJECTS')].value").value(contains(2)))
                .andExpect(jsonPath("$.limits[?(@.code == 'MAX_USERS_TOTAL')].value").value(contains(250)))
                .andExpect(jsonPath("$.limits[?(@.code == 'MAX_ACTIVE_SESSIONS_TOTAL')].value").value(contains(500)));
    }

    @Test
    void shouldIgnoreOmittedPlanLimitFieldsAndApplyExplicitNull() throws Exception {
        mockMvc.perform(put("/plans/FREE/limits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "MAX_PROJECTS": 10,
                                  "MAX_USERS_TOTAL": 20,
                                  "MAX_ACTIVE_SESSIONS_TOTAL": 30
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(put("/plans/FREE/limits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "MAX_PROJECTS": 11
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("FREE"))
                .andExpect(jsonPath("$.limits[?(@.code == 'MAX_PROJECTS')].value").value(contains(11)))
                .andExpect(jsonPath("$.limits[?(@.code == 'MAX_USERS_TOTAL')].value").value(contains(20)))
                .andExpect(jsonPath("$.limits[?(@.code == 'MAX_ACTIVE_SESSIONS_TOTAL')].value").value(contains(30)));

        mockMvc.perform(put("/plans/FREE/limits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "MAX_ACTIVE_SESSIONS_TOTAL": null
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("FREE"))
                .andExpect(jsonPath("$.limits[?(@.code == 'MAX_PROJECTS')].value").value(contains(11)))
                .andExpect(jsonPath("$.limits[?(@.code == 'MAX_USERS_TOTAL')].value").value(contains(20)))
                .andExpect(jsonPath("$.limits[?(@.code == 'MAX_ACTIVE_SESSIONS_TOTAL')].value").value(contains(nullValue())));
    }
}
