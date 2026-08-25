package com.samuelmaia1_github.yourauth.domain.auth;

import com.samuelmaia1_github.yourauth.domain.projectapikey.ProjectApiKeyScope;

import java.util.Set;

public record AuthenticatedProjectApiKey(
        String id,
        String projectId,
        String keyId,
        Set<ProjectApiKeyScope> scopes
) {
}
