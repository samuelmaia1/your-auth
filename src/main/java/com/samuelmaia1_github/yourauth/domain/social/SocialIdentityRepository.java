package com.samuelmaia1_github.yourauth.domain.social;

import java.util.Optional;

public interface SocialIdentityRepository {
    SocialIdentity save(SocialIdentity identity);

    Optional<SocialIdentity> findByProviderAndProviderUserId(SocialProvider provider, String providerUserId);

    Optional<SocialIdentity> findByAccountIdAndProvider(String accountId, SocialProvider provider);

    boolean existsByAccountId(String accountId);
}
