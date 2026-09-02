package com.samuelmaia1_github.yourauth.domain.account;

import com.samuelmaia1_github.yourauth.domain.project.ProjectEnvironment;
import com.samuelmaia1_github.yourauth.domain.project.ProjectStatus;
import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMemberRole;

import java.time.LocalDateTime;

public record AccountProjectSummary(
        String id,
        String name,
        String description,
        String ownerAccountId,
        ProjectStatus status,
        ProjectEnvironment environment,
        String tokenAudience,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        ProjectMemberRole role,
        long totalUsers,
        long totalActiveSessions
) {
}
