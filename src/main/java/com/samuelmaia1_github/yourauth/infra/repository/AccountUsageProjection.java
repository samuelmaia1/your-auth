package com.samuelmaia1_github.yourauth.infra.repository;

public record AccountUsageProjection(
        Long totalProjects,
        Long totalUsers,
        Long totalActiveSessions
) {
}
