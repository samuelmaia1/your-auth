package com.samuelmaia1_github.yourauth.domain.usersession;

import com.samuelmaia1_github.yourauth.domain.auth.exceptions.MaxActiveSessionsExceededException;
import com.samuelmaia1_github.yourauth.domain.project.authconfig.AuthConfig;
import com.samuelmaia1_github.yourauth.domain.project.authconfig.SessionMode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserSessionPolicy {
    private final UserSessionRepository userSessionRepository;

    public void ensureMaxSessionsAreNotExceeded(AuthConfig authConfig, String projectId, String userId) {
        long currentSessionsQuantity = userSessionRepository.countByProjectIdAndUserIdAndRevokedAtIsNull(projectId, userId);

        boolean isLimitExceeded = (authConfig.getSessionMode().equals(SessionMode.SINGLE_ACTIVE_SESSION) && currentSessionsQuantity >= 1)
                || authConfig.getSessionMode().equals(SessionMode.LIMITED_ACTIVE_SESSIONS) && currentSessionsQuantity >= authConfig.getMaxActiveSessions();

        if  (isLimitExceeded)
            throw new MaxActiveSessionsExceededException("Limite de logins ativos excedido");
    }
}
