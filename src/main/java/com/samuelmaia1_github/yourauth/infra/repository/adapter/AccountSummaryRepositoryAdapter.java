package com.samuelmaia1_github.yourauth.infra.repository.adapter;

import com.samuelmaia1_github.yourauth.domain.account.AccountProjectSummary;
import com.samuelmaia1_github.yourauth.domain.account.AccountSummaryRepository;
import com.samuelmaia1_github.yourauth.infra.repository.AccountProjectSummaryProjection;
import com.samuelmaia1_github.yourauth.infra.repository.AccountSummaryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AccountSummaryRepositoryAdapter implements AccountSummaryRepository {
    private final AccountSummaryJpaRepository repository;

    @Override
    public List<AccountProjectSummary> findProjectSummariesByAccountId(String accountId) {
        return repository.findProjectSummariesByAccountId(accountId)
                .stream()
                .map(AccountSummaryRepositoryAdapter::toDomain)
                .toList();
    }

    private static AccountProjectSummary toDomain(AccountProjectSummaryProjection projection) {
        return new AccountProjectSummary(
                projection.id(),
                projection.name(),
                projection.description(),
                projection.ownerAccountId(),
                projection.status(),
                projection.environment(),
                projection.tokenAudience(),
                projection.createdAt(),
                projection.updatedAt(),
                projection.role(),
                count(projection.totalUsers()),
                count(projection.totalActiveSessions())
        );
    }

    private static long count(Long value) {
        return value == null ? 0L : value;
    }
}
