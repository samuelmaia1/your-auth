package com.samuelmaia1_github.yourauth.domain.account;

public record AccountUsage(
        long totalProjects,
        long totalUsers,
        long totalActiveSessions
) {
}
