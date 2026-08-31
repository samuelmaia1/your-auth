package com.samuelmaia1_github.yourauth.domain.projectapikey;

import com.samuelmaia1_github.yourauth.domain.shared.PageResult;
import com.samuelmaia1_github.yourauth.domain.shared.Pagination;

import java.util.Optional;

public interface ProjectApiKeyRepository {
    ProjectApiKey save(ProjectApiKey apiKey);

    Optional<ProjectApiKey> findById(String id);

    Optional<ProjectApiKey> findByProjectIdAndId(String projectId, String id);

    Optional<ProjectApiKey> findByKeyId(String keyId);

    Optional<ProjectApiKey> findByPrefix(String prefix);

    PageResult<ProjectApiKey> findAllByProjectId(String projectId, Pagination pagination);

    void deleteById(String id);
}
