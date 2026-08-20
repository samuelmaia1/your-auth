package com.samuelmaia1_github.yourauth.domain.project.passwordconfig;

import java.util.Optional;

public interface PasswordConfigRepository {
    PasswordConfig save(PasswordConfig config);
    Optional<PasswordConfig> findByProjectId(String projectId);
}
