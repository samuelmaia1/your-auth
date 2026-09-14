package com.samuelmaia1_github.yourauth.presentation.dto.project;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.samuelmaia1_github.yourauth.domain.project.ProjectEnvironment;
import com.samuelmaia1_github.yourauth.domain.project.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;

@Schema(description = "Campos omitidos nao alteram o projeto atual. Campo description enviado como null remove a descricao.")
public class UpdateProjectDTO {
    @Size(max = 100)
    private String name;

    @Size(max = 255)
    private String description;

    private ProjectStatus status;

    private ProjectEnvironment environment;

    @Size(max = 255)
    private String tokenAudience;

    private boolean nameProvided;
    private boolean descriptionProvided;
    private boolean statusProvided;
    private boolean environmentProvided;
    private boolean tokenAudienceProvided;

    @JsonProperty("name")
    public String name() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
        this.nameProvided = true;
    }

    @JsonIgnore
    public boolean nameProvided() {
        return nameProvided;
    }

    @JsonProperty("description")
    public String description() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
        this.descriptionProvided = true;
    }

    @JsonIgnore
    public boolean descriptionProvided() {
        return descriptionProvided;
    }

    @JsonProperty("status")
    public ProjectStatus status() {
        return status;
    }

    @JsonProperty("status")
    public void setStatus(ProjectStatus status) {
        this.status = status;
        this.statusProvided = true;
    }

    @JsonIgnore
    public boolean statusProvided() {
        return statusProvided;
    }

    @JsonProperty("environment")
    public ProjectEnvironment environment() {
        return environment;
    }

    @JsonProperty("environment")
    public void setEnvironment(ProjectEnvironment environment) {
        this.environment = environment;
        this.environmentProvided = true;
    }

    @JsonIgnore
    public boolean environmentProvided() {
        return environmentProvided;
    }

    @JsonProperty("tokenAudience")
    public String tokenAudience() {
        return tokenAudience;
    }

    @JsonProperty("tokenAudience")
    public void setTokenAudience(String tokenAudience) {
        this.tokenAudience = tokenAudience;
        this.tokenAudienceProvided = true;
    }

    @JsonIgnore
    public boolean tokenAudienceProvided() {
        return tokenAudienceProvided;
    }

    @JsonIgnore
    @AssertTrue(message = "O nome é obrigatório")
    public boolean isNameValidWhenProvided() {
        return !nameProvided || (name != null && !name.isBlank());
    }

    @JsonIgnore
    @AssertTrue(message = "O status é obrigatório")
    public boolean isStatusValidWhenProvided() {
        return !statusProvided || status != null;
    }

    @JsonIgnore
    @AssertTrue(message = "O ambiente é obrigatório")
    public boolean isEnvironmentValidWhenProvided() {
        return !environmentProvided || environment != null;
    }

    @JsonIgnore
    @AssertTrue(message = "A audiência do token é obrigatória")
    public boolean isTokenAudienceValidWhenProvided() {
        return !tokenAudienceProvided || (tokenAudience != null && !tokenAudience.isBlank());
    }
}
