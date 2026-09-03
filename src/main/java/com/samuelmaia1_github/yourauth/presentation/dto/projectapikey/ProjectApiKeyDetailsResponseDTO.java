package com.samuelmaia1_github.yourauth.presentation.dto.projectapikey;

import com.samuelmaia1_github.yourauth.domain.projectapikey.ProjectApiKeyScope;
import com.samuelmaia1_github.yourauth.presentation.dto.account.AccountBasicResponseDTO;

import java.time.LocalDateTime;
import java.util.Set;

public record ProjectApiKeyDetailsResponseDTO(
        String id,
        String projectId,
        String name,
        String keyId,
        String prefix,
        String secretLastFour,
        String environment,
        Set<ProjectApiKeyScope> scopes,
        String createdByAccountId,
        AccountBasicResponseDTO createdByAccount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime lastUsedAt,
        LocalDateTime revokedAt,
        LocalDateTime expiresAt
) {
}
