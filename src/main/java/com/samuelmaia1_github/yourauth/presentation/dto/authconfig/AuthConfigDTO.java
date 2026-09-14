package com.samuelmaia1_github.yourauth.presentation.dto.authconfig;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.samuelmaia1_github.yourauth.domain.project.authconfig.SessionMode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Schema(description = "Em updates, campos omitidos nao alteram a configuracao atual. Campo maxActiveSessions enviado como null remove o limite quando o modo de sessao permitir.")
public class AuthConfigDTO {
    @Min(value = 1, message = "A expiração do access token deve ser de pelo menos 1 minuto")
    @Max(value = 1440, message = "A expiração do access token deve ser de no máximo 1440 minutos")
    private Integer accessTokenExpirationMinutes;

    @Min(value = 1, message = "A expiração do refresh token deve ser de pelo menos 1 dia")
    @Max(value = 365, message = "A expiração do refresh token deve ser de no máximo 365 dias")
    private Integer refreshTokenExpirationDays;

    private SessionMode sessionMode;

    @Min(value = 1, message = "O limite de sessões ativas deve ser de pelo menos 1")
    @Max(value = 100, message = "O limite de sessões ativas deve ser de no máximo 100")
    private Integer maxActiveSessions;

    private Boolean refreshTokenRotationEnabled;
    private Boolean revokeTokensOnPasswordChange;

    @Min(value = 1, message = "O limite de tentativas de login deve ser de pelo menos 1")
    @Max(value = 20, message = "O limite de tentativas de login deve ser de no máximo 20")
    private Integer failedLoginAttemptsLimit;

    @Min(value = 1, message = "A duração do bloqueio deve ser de pelo menos 1 minuto")
    @Max(value = 1440, message = "A duração do bloqueio deve ser de no máximo 1440 minutos")
    private Integer lockDurationMinutes;

    private Boolean requireEmailVerification;
    private Boolean registrationEnabled;

    private boolean accessTokenExpirationMinutesProvided;
    private boolean refreshTokenExpirationDaysProvided;
    private boolean sessionModeProvided;
    private boolean maxActiveSessionsProvided;
    private boolean refreshTokenRotationEnabledProvided;
    private boolean revokeTokensOnPasswordChangeProvided;
    private boolean failedLoginAttemptsLimitProvided;
    private boolean lockDurationMinutesProvided;
    private boolean requireEmailVerificationProvided;
    private boolean registrationEnabledProvided;

    public AuthConfigDTO() {
    }

    public AuthConfigDTO(
            Integer accessTokenExpirationMinutes,
            Integer refreshTokenExpirationDays,
            SessionMode sessionMode,
            Integer maxActiveSessions,
            Boolean refreshTokenRotationEnabled,
            Boolean revokeTokensOnPasswordChange,
            Integer failedLoginAttemptsLimit,
            Integer lockDurationMinutes,
            Boolean requireEmailVerification,
            Boolean registrationEnabled
    ) {
        this.accessTokenExpirationMinutes = accessTokenExpirationMinutes;
        this.refreshTokenExpirationDays = refreshTokenExpirationDays;
        this.sessionMode = sessionMode;
        this.maxActiveSessions = maxActiveSessions;
        this.refreshTokenRotationEnabled = refreshTokenRotationEnabled;
        this.revokeTokensOnPasswordChange = revokeTokensOnPasswordChange;
        this.failedLoginAttemptsLimit = failedLoginAttemptsLimit;
        this.lockDurationMinutes = lockDurationMinutes;
        this.requireEmailVerification = requireEmailVerification;
        this.registrationEnabled = registrationEnabled;
    }

    @JsonProperty("accessTokenExpirationMinutes")
    public Integer accessTokenExpirationMinutes() {
        return accessTokenExpirationMinutes;
    }

    @JsonProperty("accessTokenExpirationMinutes")
    public void setAccessTokenExpirationMinutes(Integer accessTokenExpirationMinutes) {
        this.accessTokenExpirationMinutes = accessTokenExpirationMinutes;
        this.accessTokenExpirationMinutesProvided = true;
    }

    @JsonIgnore
    public boolean accessTokenExpirationMinutesProvided() {
        return accessTokenExpirationMinutesProvided;
    }

    @JsonProperty("refreshTokenExpirationDays")
    public Integer refreshTokenExpirationDays() {
        return refreshTokenExpirationDays;
    }

    @JsonProperty("refreshTokenExpirationDays")
    public void setRefreshTokenExpirationDays(Integer refreshTokenExpirationDays) {
        this.refreshTokenExpirationDays = refreshTokenExpirationDays;
        this.refreshTokenExpirationDaysProvided = true;
    }

    @JsonIgnore
    public boolean refreshTokenExpirationDaysProvided() {
        return refreshTokenExpirationDaysProvided;
    }

    @JsonProperty("sessionMode")
    public SessionMode sessionMode() {
        return sessionMode;
    }

    @JsonProperty("sessionMode")
    public void setSessionMode(SessionMode sessionMode) {
        this.sessionMode = sessionMode;
        this.sessionModeProvided = true;
    }

    @JsonIgnore
    public boolean sessionModeProvided() {
        return sessionModeProvided;
    }

    @JsonProperty("maxActiveSessions")
    public Integer maxActiveSessions() {
        return maxActiveSessions;
    }

    @JsonProperty("maxActiveSessions")
    public void setMaxActiveSessions(Integer maxActiveSessions) {
        this.maxActiveSessions = maxActiveSessions;
        this.maxActiveSessionsProvided = true;
    }

    @JsonIgnore
    public boolean maxActiveSessionsProvided() {
        return maxActiveSessionsProvided;
    }

    @JsonProperty("refreshTokenRotationEnabled")
    public Boolean refreshTokenRotationEnabled() {
        return refreshTokenRotationEnabled;
    }

    @JsonProperty("refreshTokenRotationEnabled")
    public void setRefreshTokenRotationEnabled(Boolean refreshTokenRotationEnabled) {
        this.refreshTokenRotationEnabled = refreshTokenRotationEnabled;
        this.refreshTokenRotationEnabledProvided = true;
    }

    @JsonIgnore
    public boolean refreshTokenRotationEnabledProvided() {
        return refreshTokenRotationEnabledProvided;
    }

    @JsonProperty("revokeTokensOnPasswordChange")
    public Boolean revokeTokensOnPasswordChange() {
        return revokeTokensOnPasswordChange;
    }

    @JsonProperty("revokeTokensOnPasswordChange")
    public void setRevokeTokensOnPasswordChange(Boolean revokeTokensOnPasswordChange) {
        this.revokeTokensOnPasswordChange = revokeTokensOnPasswordChange;
        this.revokeTokensOnPasswordChangeProvided = true;
    }

    @JsonIgnore
    public boolean revokeTokensOnPasswordChangeProvided() {
        return revokeTokensOnPasswordChangeProvided;
    }

    @JsonProperty("failedLoginAttemptsLimit")
    public Integer failedLoginAttemptsLimit() {
        return failedLoginAttemptsLimit;
    }

    @JsonProperty("failedLoginAttemptsLimit")
    public void setFailedLoginAttemptsLimit(Integer failedLoginAttemptsLimit) {
        this.failedLoginAttemptsLimit = failedLoginAttemptsLimit;
        this.failedLoginAttemptsLimitProvided = true;
    }

    @JsonIgnore
    public boolean failedLoginAttemptsLimitProvided() {
        return failedLoginAttemptsLimitProvided;
    }

    @JsonProperty("lockDurationMinutes")
    public Integer lockDurationMinutes() {
        return lockDurationMinutes;
    }

    @JsonProperty("lockDurationMinutes")
    public void setLockDurationMinutes(Integer lockDurationMinutes) {
        this.lockDurationMinutes = lockDurationMinutes;
        this.lockDurationMinutesProvided = true;
    }

    @JsonIgnore
    public boolean lockDurationMinutesProvided() {
        return lockDurationMinutesProvided;
    }

    @JsonProperty("requireEmailVerification")
    public Boolean requireEmailVerification() {
        return requireEmailVerification;
    }

    @JsonProperty("requireEmailVerification")
    public void setRequireEmailVerification(Boolean requireEmailVerification) {
        this.requireEmailVerification = requireEmailVerification;
        this.requireEmailVerificationProvided = true;
    }

    @JsonIgnore
    public boolean requireEmailVerificationProvided() {
        return requireEmailVerificationProvided;
    }

    @JsonProperty("registrationEnabled")
    public Boolean registrationEnabled() {
        return registrationEnabled;
    }

    @JsonProperty("registrationEnabled")
    public void setRegistrationEnabled(Boolean registrationEnabled) {
        this.registrationEnabled = registrationEnabled;
        this.registrationEnabledProvided = true;
    }

    @JsonIgnore
    public boolean registrationEnabledProvided() {
        return registrationEnabledProvided;
    }

    @JsonIgnore
    @AssertTrue(message = "A expiração do access token é obrigatória quando informada.")
    public boolean isAccessTokenExpirationMinutesValidWhenProvided() {
        return !accessTokenExpirationMinutesProvided || accessTokenExpirationMinutes != null;
    }

    @JsonIgnore
    @AssertTrue(message = "A expiração do refresh token é obrigatória quando informada.")
    public boolean isRefreshTokenExpirationDaysValidWhenProvided() {
        return !refreshTokenExpirationDaysProvided || refreshTokenExpirationDays != null;
    }

    @JsonIgnore
    @AssertTrue(message = "O modo de sessão é obrigatório quando informado.")
    public boolean isSessionModeValidWhenProvided() {
        return !sessionModeProvided || sessionMode != null;
    }

    @JsonIgnore
    @AssertTrue(message = "A rotação de refresh token é obrigatória quando informada.")
    public boolean isRefreshTokenRotationEnabledValidWhenProvided() {
        return !refreshTokenRotationEnabledProvided || refreshTokenRotationEnabled != null;
    }

    @JsonIgnore
    @AssertTrue(message = "A revogação de tokens na troca de senha é obrigatória quando informada.")
    public boolean isRevokeTokensOnPasswordChangeValidWhenProvided() {
        return !revokeTokensOnPasswordChangeProvided || revokeTokensOnPasswordChange != null;
    }

    @JsonIgnore
    @AssertTrue(message = "O limite de tentativas de login é obrigatório quando informado.")
    public boolean isFailedLoginAttemptsLimitValidWhenProvided() {
        return !failedLoginAttemptsLimitProvided || failedLoginAttemptsLimit != null;
    }

    @JsonIgnore
    @AssertTrue(message = "A duração do bloqueio é obrigatória quando informada.")
    public boolean isLockDurationMinutesValidWhenProvided() {
        return !lockDurationMinutesProvided || lockDurationMinutes != null;
    }

    @JsonIgnore
    @AssertTrue(message = "A exigência de verificação de e-mail é obrigatória quando informada.")
    public boolean isRequireEmailVerificationValidWhenProvided() {
        return !requireEmailVerificationProvided || requireEmailVerification != null;
    }

    @JsonIgnore
    @AssertTrue(message = "A habilitação de cadastro é obrigatória quando informada.")
    public boolean isRegistrationEnabledValidWhenProvided() {
        return !registrationEnabledProvided || registrationEnabled != null;
    }
}
