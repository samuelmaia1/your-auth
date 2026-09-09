package com.samuelmaia1_github.yourauth.domain.account;

import com.samuelmaia1_github.yourauth.infra.cache.names.AccountCacheNames;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountUsageService {
    private final AccountUsageRepository repository;

    @Cacheable(
            cacheNames = AccountCacheNames.ACCOUNT_USAGE_BY_OWNER_ACCOUNT_ID,
            key = "#accountId"
    )
    public AccountUsage findByOwnerAccountId(String accountId) {
        return repository.findByOwnerAccountId(accountId);
    }
}
