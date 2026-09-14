package com.samuelmaia1_github.yourauth.presentation.dto.passwordconfig;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Schema(description = "Em updates, campos omitidos nao alteram a configuracao atual.")
public class PasswordConfigDTO {
    @Min(value = 1, message = "O tamanho mínimo deve ser pelo menos 1")
    @Max(value = 120, message = "O tamanho mínimo deve ser no máximo 120")
    private Integer minSize;

    @Min(value = 1, message = "O tamanho máximo deve ser pelo menos 1")
    @Max(value = 120, message = "O tamanho máximo deve ser no máximo 120")
    private Integer maxSize;

    private Boolean numberRequired;
    private Boolean uppercaseRequired;
    private Boolean lowercaseRequired;
    private Boolean specialCharRequired;

    private boolean minSizeProvided;
    private boolean maxSizeProvided;
    private boolean numberRequiredProvided;
    private boolean uppercaseRequiredProvided;
    private boolean lowercaseRequiredProvided;
    private boolean specialCharRequiredProvided;

    public PasswordConfigDTO() {
    }

    public PasswordConfigDTO(
            Integer minSize,
            Integer maxSize,
            Boolean numberRequired,
            Boolean uppercaseRequired,
            Boolean lowercaseRequired,
            Boolean specialCharRequired
    ) {
        this.minSize = minSize;
        this.maxSize = maxSize;
        this.numberRequired = numberRequired;
        this.uppercaseRequired = uppercaseRequired;
        this.lowercaseRequired = lowercaseRequired;
        this.specialCharRequired = specialCharRequired;
    }

    @JsonProperty("minSize")
    public Integer minSize() {
        return minSize;
    }

    @JsonProperty("minSize")
    public void setMinSize(Integer minSize) {
        this.minSize = minSize;
        this.minSizeProvided = true;
    }

    @JsonIgnore
    public boolean minSizeProvided() {
        return minSizeProvided;
    }

    @JsonProperty("maxSize")
    public Integer maxSize() {
        return maxSize;
    }

    @JsonProperty("maxSize")
    public void setMaxSize(Integer maxSize) {
        this.maxSize = maxSize;
        this.maxSizeProvided = true;
    }

    @JsonIgnore
    public boolean maxSizeProvided() {
        return maxSizeProvided;
    }

    @JsonProperty("numberRequired")
    public Boolean numberRequired() {
        return numberRequired;
    }

    @JsonProperty("numberRequired")
    public void setNumberRequired(Boolean numberRequired) {
        this.numberRequired = numberRequired;
        this.numberRequiredProvided = true;
    }

    @JsonIgnore
    public boolean numberRequiredProvided() {
        return numberRequiredProvided;
    }

    @JsonProperty("uppercaseRequired")
    public Boolean uppercaseRequired() {
        return uppercaseRequired;
    }

    @JsonProperty("uppercaseRequired")
    public void setUppercaseRequired(Boolean uppercaseRequired) {
        this.uppercaseRequired = uppercaseRequired;
        this.uppercaseRequiredProvided = true;
    }

    @JsonIgnore
    public boolean uppercaseRequiredProvided() {
        return uppercaseRequiredProvided;
    }

    @JsonProperty("lowercaseRequired")
    public Boolean lowercaseRequired() {
        return lowercaseRequired;
    }

    @JsonProperty("lowercaseRequired")
    public void setLowercaseRequired(Boolean lowercaseRequired) {
        this.lowercaseRequired = lowercaseRequired;
        this.lowercaseRequiredProvided = true;
    }

    @JsonIgnore
    public boolean lowercaseRequiredProvided() {
        return lowercaseRequiredProvided;
    }

    @JsonProperty("specialCharRequired")
    public Boolean specialCharRequired() {
        return specialCharRequired;
    }

    @JsonProperty("specialCharRequired")
    public void setSpecialCharRequired(Boolean specialCharRequired) {
        this.specialCharRequired = specialCharRequired;
        this.specialCharRequiredProvided = true;
    }

    @JsonIgnore
    public boolean specialCharRequiredProvided() {
        return specialCharRequiredProvided;
    }

    @JsonIgnore
    @AssertTrue(message = "O tamanho mínimo é obrigatório quando informado.")
    public boolean isMinSizeValidWhenProvided() {
        return !minSizeProvided || minSize != null;
    }

    @JsonIgnore
    @AssertTrue(message = "O tamanho máximo é obrigatório quando informado.")
    public boolean isMaxSizeValidWhenProvided() {
        return !maxSizeProvided || maxSize != null;
    }

    @JsonIgnore
    @AssertTrue(message = "A exigência de número é obrigatória quando informada.")
    public boolean isNumberRequiredValidWhenProvided() {
        return !numberRequiredProvided || numberRequired != null;
    }

    @JsonIgnore
    @AssertTrue(message = "A exigência de letra maiúscula é obrigatória quando informada.")
    public boolean isUppercaseRequiredValidWhenProvided() {
        return !uppercaseRequiredProvided || uppercaseRequired != null;
    }

    @JsonIgnore
    @AssertTrue(message = "A exigência de letra minúscula é obrigatória quando informada.")
    public boolean isLowercaseRequiredValidWhenProvided() {
        return !lowercaseRequiredProvided || lowercaseRequired != null;
    }

    @JsonIgnore
    @AssertTrue(message = "A exigência de caractere especial é obrigatória quando informada.")
    public boolean isSpecialCharRequiredValidWhenProvided() {
        return !specialCharRequiredProvided || specialCharRequired != null;
    }

    @JsonIgnore
    @AssertTrue(message = "O tamanho mínimo não pode ser maior que o tamanho máximo.")
    public boolean isValidRange() {
        if (minSize == null || maxSize == null) return true;
        return minSize <= maxSize;
    }
}
