package com.samuelmaia1_github.yourauth.domain.project.authconfig;

public record AuthConfigUpdate(
        Integer accessTokenExpirationMinutes,
        boolean accessTokenExpirationMinutesProvided,
        Integer refreshTokenExpirationDays,
        boolean refreshTokenExpirationDaysProvided,
        SessionMode sessionMode,
        boolean sessionModeProvided,
        Integer maxActiveSessions,
        boolean maxActiveSessionsProvided,
        Boolean refreshTokenRotationEnabled,
        boolean refreshTokenRotationEnabledProvided,
        Boolean revokeTokensOnPasswordChange,
        boolean revokeTokensOnPasswordChangeProvided,
        Integer failedLoginAttemptsLimit,
        boolean failedLoginAttemptsLimitProvided,
        Integer lockDurationMinutes,
        boolean lockDurationMinutesProvided,
        Boolean requireEmailVerification,
        boolean requireEmailVerificationProvided,
        Boolean registrationEnabled,
        boolean registrationEnabledProvided
) {
}
