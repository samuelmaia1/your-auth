package com.samuelmaia1_github.yourauth.presentation.dto.account;

import java.util.List;

public record AccountSummaryResponseDTO(
        long totalProjects,
        long totalUsers,
        long totalActiveSessions,
        List<AccountProjectSummaryResponseDTO> projects
) {
}
