package com.samuelmaia1_github.yourauth.domain.account;

public interface AccountUsageRepository {
    AccountUsage findByOwnerAccountId(String accountId);
}
