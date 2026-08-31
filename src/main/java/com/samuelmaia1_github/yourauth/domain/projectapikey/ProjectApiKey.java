package com.samuelmaia1_github.yourauth.domain.projectapikey;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectApiKey {
    private String id;
    private String projectId;
    private String name;
    private String keyId;
    private String prefix;
    private String secretHash;
    private String secretLastFour;
    private String environment;
    private Set<ProjectApiKeyScope> scopes;
    private String createdByAccountId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastUsedAt;
    private LocalDateTime revokedAt;
    private LocalDateTime expiresAt;

    public void revoke() {
        this.revokedAt = LocalDateTime.now();
    }

    public void markAsUsed() {
        this.lastUsedAt = LocalDateTime.now();
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !isRevoked() && !isExpired();
    }

    public boolean hasScope(ProjectApiKeyScope scope) {
        return scopes != null && scopes.contains(scope);
    }
}
