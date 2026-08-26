package com.samuelmaia1_github.yourauth.presentation.dto.authconfig;

import com.samuelmaia1_github.yourauth.domain.project.authconfig.SessionMode;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record AuthConfigDTO(
        @Min(value = 1, message = "A expiração do access token deve ser de pelo menos 1 minuto")
        @Max(value = 1440, message = "A expiração do access token deve ser de no máximo 1440 minutos")
        Integer accessTokenExpirationMinutes,

        @Min(value = 1, message = "A expiração do refresh token deve ser de pelo menos 1 dia")
        @Max(value = 365, message = "A expiração do refresh token deve ser de no máximo 365 dias")
        Integer refreshTokenExpirationDays,

        SessionMode sessionMode,

        @Min(value = 1, message = "O limite de sessões ativas deve ser de pelo menos 1")
        @Max(value = 100, message = "O limite de sessões ativas deve ser de no máximo 100")
        Integer maxActiveSessions,

        Boolean refreshTokenRotationEnabled,

        Boolean revokeTokensOnPasswordChange,

        @Min(value = 1, message = "O limite de tentativas de login deve ser de pelo menos 1")
        @Max(value = 20, message = "O limite de tentativas de login deve ser de no máximo 20")
        Integer failedLoginAttemptsLimit,

        @Min(value = 1, message = "A duração do bloqueio deve ser de pelo menos 1 minuto")
        @Max(value = 1440, message = "A duração do bloqueio deve ser de no máximo 1440 minutos")
        Integer lockDurationMinutes,

        Boolean requireEmailVerification,

        Boolean registrationEnabled
) {
    @AssertTrue(message = "O limite de sessões ativas é obrigatório para o modo LIMITED_ACTIVE_SESSIONS.")
    public boolean isMaxActiveSessionsRequiredWhenLimited() {
        return !SessionMode.LIMITED_ACTIVE_SESSIONS.equals(sessionMode) || maxActiveSessions != null;
    }
}
