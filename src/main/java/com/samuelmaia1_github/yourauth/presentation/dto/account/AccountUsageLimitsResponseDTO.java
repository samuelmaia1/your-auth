package com.samuelmaia1_github.yourauth.presentation.dto.account;

public record AccountUsageLimitsResponseDTO(
        Long maxProjects,
        Long maxUsersTotal,
        Long maxActiveSessionsTotal
) {
}
