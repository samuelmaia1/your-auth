package com.samuelmaia1_github.yourauth.presentation.dto.plan;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

@Schema(description = "Campos omitidos nao alteram o limite atual. Campos enviados como null deixam o limite ilimitado.")
public class UpdatePlanLimitsDTO {
    @Min(value = 0, message = "O limite de projetos deve ser maior ou igual a 0")
    private Long maxProjects;

    @Min(value = 0, message = "O limite de usuários totais deve ser maior ou igual a 0")
    private Long maxUsersTotal;

    @Min(value = 0, message = "O limite de sessões ativas totais deve ser maior ou igual a 0")
    private Long maxActiveSessionsTotal;

    private boolean maxProjectsProvided;
    private boolean maxUsersTotalProvided;
    private boolean maxActiveSessionsTotalProvided;

    @Schema(description = "Limite maximo de projetos.", nullable = true, minimum = "0")
    @JsonProperty("MAX_PROJECTS")
    public Long maxProjects() {
        return maxProjects;
    }

    @JsonProperty("MAX_PROJECTS")
    @JsonAlias("maxProjects")
    public void setMaxProjects(Long maxProjects) {
        this.maxProjects = maxProjects;
        this.maxProjectsProvided = true;
    }

    @JsonIgnore
    public boolean maxProjectsProvided() {
        return maxProjectsProvided;
    }

    @Schema(description = "Limite maximo de usuarios totais.", nullable = true, minimum = "0")
    @JsonProperty("MAX_USERS_TOTAL")
    public Long maxUsersTotal() {
        return maxUsersTotal;
    }

    @JsonProperty("MAX_USERS_TOTAL")
    @JsonAlias("maxUsersTotal")
    public void setMaxUsersTotal(Long maxUsersTotal) {
        this.maxUsersTotal = maxUsersTotal;
        this.maxUsersTotalProvided = true;
    }

    @JsonIgnore
    public boolean maxUsersTotalProvided() {
        return maxUsersTotalProvided;
    }

    @Schema(description = "Limite maximo de sessoes ativas totais.", nullable = true, minimum = "0")
    @JsonProperty("MAX_ACTIVE_SESSIONS_TOTAL")
    public Long maxActiveSessionsTotal() {
        return maxActiveSessionsTotal;
    }

    @JsonProperty("MAX_ACTIVE_SESSIONS_TOTAL")
    @JsonAlias("maxActiveSessionsTotal")
    public void setMaxActiveSessionsTotal(Long maxActiveSessionsTotal) {
        this.maxActiveSessionsTotal = maxActiveSessionsTotal;
        this.maxActiveSessionsTotalProvided = true;
    }

    @JsonIgnore
    public boolean maxActiveSessionsTotalProvided() {
        return maxActiveSessionsTotalProvided;
    }
}
