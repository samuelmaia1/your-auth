package com.samuelmaia1_github.yourauth.presentation.mapper;

import com.samuelmaia1_github.yourauth.domain.account.AccountProjectSummary;
import com.samuelmaia1_github.yourauth.domain.account.AccountSummary;
import com.samuelmaia1_github.yourauth.presentation.dto.account.AccountProjectSummaryResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.account.AccountSummaryResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class AccountSummaryPresentationMapper {
    public static AccountSummaryResponseDTO toResponseDTO(AccountSummary summary) {
        return new AccountSummaryResponseDTO(
                summary.totalProjects(),
                summary.totalUsers(),
                summary.totalActiveSessions(),
                summary.projects().stream()
                        .map(AccountSummaryPresentationMapper::toProjectResponseDTO)
                        .toList()
        );
    }

    private static AccountProjectSummaryResponseDTO toProjectResponseDTO(AccountProjectSummary project) {
        return new AccountProjectSummaryResponseDTO(
                project.id(),
                project.name(),
                project.description(),
                project.ownerAccountId(),
                project.status(),
                project.environment(),
                project.tokenAudience(),
                project.createdAt(),
                project.updatedAt(),
                project.role(),
                project.totalUsers(),
                project.totalActiveSessions()
        );
    }
}
