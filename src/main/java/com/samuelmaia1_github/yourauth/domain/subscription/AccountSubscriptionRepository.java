package com.samuelmaia1_github.yourauth.domain.subscription;

import java.util.Optional;

public interface AccountSubscriptionRepository {
    AccountSubscription save(AccountSubscription subscription);

    Optional<AccountSubscription> findById(String id);

    Optional<AccountSubscription> findCurrentByAccountId(String accountId);

    void saveCurrent(String accountId, String subscriptionId);
}
