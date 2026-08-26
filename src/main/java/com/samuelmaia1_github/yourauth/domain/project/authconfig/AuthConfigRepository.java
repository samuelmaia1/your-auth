package com.samuelmaia1_github.yourauth.domain.project.authconfig;

import java.util.Optional;

public interface AuthConfigRepository {
    AuthConfig save(AuthConfig config);

    Optional<AuthConfig> findByProjectId(String projectId);
}
