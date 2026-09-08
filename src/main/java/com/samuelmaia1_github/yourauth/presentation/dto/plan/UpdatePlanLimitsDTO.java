package com.samuelmaia1_github.yourauth.presentation.dto.plan;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;

public record UpdatePlanLimitsDTO(
        @JsonProperty("MAX_PROJECTS")
        @JsonAlias("maxProjects")
        @Min(value = 0, message = "O limite de projetos deve ser maior ou igual a 0")
        Long maxProjects,

        @JsonProperty("MAX_USERS_TOTAL")
        @JsonAlias("maxUsersTotal")
        @Min(value = 0, message = "O limite de usuários totais deve ser maior ou igual a 0")
        Long maxUsersTotal,

        @JsonProperty("MAX_ACTIVE_SESSIONS_TOTAL")
        @JsonAlias("maxActiveSessionsTotal")
        @Min(value = 0, message = "O limite de sessões ativas totais deve ser maior ou igual a 0")
        Long maxActiveSessionsTotal
) {
}
