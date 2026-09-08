package com.samuelmaia1_github.yourauth.presentation.dto.account;

public record AccountUsageTotalsResponseDTO(
        long totalProjects,
        long totalUsers,
        long totalActiveSessions
) {
}
