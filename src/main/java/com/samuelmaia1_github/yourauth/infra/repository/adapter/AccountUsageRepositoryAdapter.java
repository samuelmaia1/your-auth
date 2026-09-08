package com.samuelmaia1_github.yourauth.infra.repository.adapter;

import com.samuelmaia1_github.yourauth.domain.account.AccountUsage;
import com.samuelmaia1_github.yourauth.domain.account.AccountUsageRepository;
import com.samuelmaia1_github.yourauth.infra.repository.AccountUsageJpaRepository;
import com.samuelmaia1_github.yourauth.infra.repository.AccountUsageProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountUsageRepositoryAdapter implements AccountUsageRepository {
    private final AccountUsageJpaRepository repository;

    @Override
    public AccountUsage findByOwnerAccountId(String accountId) {
        return toDomain(repository.findByOwnerAccountId(accountId));
    }

    private static AccountUsage toDomain(AccountUsageProjection projection) {
        if (projection == null) {
            return new AccountUsage(0L, 0L, 0L);
        }

        return new AccountUsage(
                count(projection.totalProjects()),
                count(projection.totalUsers()),
                count(projection.totalActiveSessions())
        );
    }

    private static long count(Long value) {
        return value == null ? 0L : value;
    }
}
