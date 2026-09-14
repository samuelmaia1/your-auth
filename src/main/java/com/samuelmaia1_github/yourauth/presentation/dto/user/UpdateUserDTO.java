package com.samuelmaia1_github.yourauth.presentation.dto.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.samuelmaia1_github.yourauth.presentation.dto.shared.PhoneDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

@Schema(description = "Campos omitidos nao alteram o usuario atual. Campo phone enviado como null remove o telefone.")
public class UpdateUserDTO {
    @Email(message = "O e-mail informado é inválido.")
    @Size(max = 320)
    private String email;

    @Size(max = 255)
    private String password;

    @Valid
    private PhoneDTO phone;

    private boolean emailProvided;
    private boolean passwordProvided;
    private boolean phoneProvided;

    @JsonProperty("email")
    public String email() {
        return email;
    }

    @JsonProperty("email")
    public void setEmail(String email) {
        this.email = email;
        this.emailProvided = true;
    }

    @JsonIgnore
    public boolean emailProvided() {
        return emailProvided;
    }

    @JsonProperty("password")
    public String password() {
        return password;
    }

    @JsonProperty("password")
    public void setPassword(String password) {
        this.password = password;
        this.passwordProvided = true;
    }

    @JsonIgnore
    public boolean passwordProvided() {
        return passwordProvided;
    }

    @JsonProperty("phone")
    public PhoneDTO phone() {
        return phone;
    }

    @JsonProperty("phone")
    public void setPhone(PhoneDTO phone) {
        this.phone = phone;
        this.phoneProvided = true;
    }

    @JsonIgnore
    public boolean phoneProvided() {
        return phoneProvided;
    }

    @JsonIgnore
    @AssertTrue(message = "O e-mail é obrigatório")
    public boolean isEmailValidWhenProvided() {
        return !emailProvided || (email != null && !email.isBlank());
    }

    @JsonIgnore
    @AssertTrue(message = "A senha é obrigatória")
    public boolean isPasswordValidWhenProvided() {
        return !passwordProvided || (password != null && !password.isBlank());
    }
}
