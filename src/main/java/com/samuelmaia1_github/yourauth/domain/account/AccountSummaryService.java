package com.samuelmaia1_github.yourauth.domain.account;

import com.samuelmaia1_github.yourauth.infra.cache.names.AccountCacheNames;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountSummaryService {
    private final AccountSummaryRepository repository;

    @Cacheable(
            cacheNames = AccountCacheNames.ACCOUNT_SUMMARY_BY_ACCOUNT_ID,
            key = "#accountId"
    )
    public AccountSummary findByAccountId(String accountId) {
        return new AccountSummary(repository.findProjectSummariesByAccountId(accountId));
    }
}
