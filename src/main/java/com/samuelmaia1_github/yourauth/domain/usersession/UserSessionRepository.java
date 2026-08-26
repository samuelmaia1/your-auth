package com.samuelmaia1_github.yourauth.domain.usersession;

import java.util.List;
import java.util.Optional;

public interface UserSessionRepository {
    UserSession save(UserSession userSession);

    Optional<UserSession> findById(String id);

    List<UserSession> findAllByProjectIdAndUserId(String projectId, String userId);

    List<UserSession> findAllByProjectIdAndUserIdAndRevokedAtIsNull(String projectId, String userId);

    void revokeById(String id);
}
