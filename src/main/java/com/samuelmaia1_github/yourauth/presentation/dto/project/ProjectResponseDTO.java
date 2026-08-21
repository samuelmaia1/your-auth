package com.samuelmaia1_github.yourauth.presentation.dto.project;

import com.samuelmaia1_github.yourauth.domain.project.ProjectEnvironment;
import com.samuelmaia1_github.yourauth.domain.project.ProjectStatus;

import java.time.LocalDateTime;

public record ProjectResponseDTO(
        String id,
        String name,
        String description,
        String ownerAccountId,
        ProjectStatus status,
        ProjectEnvironment environment,
        String tokenAudience,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
