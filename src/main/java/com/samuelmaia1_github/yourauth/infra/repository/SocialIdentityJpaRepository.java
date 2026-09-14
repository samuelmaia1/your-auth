package com.samuelmaia1_github.yourauth.infra.repository;

import com.samuelmaia1_github.yourauth.domain.social.SocialProvider;
import com.samuelmaia1_github.yourauth.infra.repository.entity.SocialIdentityEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SocialIdentityJpaRepository extends JpaRepository<SocialIdentityEntity, String> {
    Optional<SocialIdentityEntity> findByProviderAndProviderUserId(
            SocialProvider provider,
            String providerUserId
    );

    Optional<SocialIdentityEntity> findByAccountIdAndProvider(
            String accountId,
            SocialProvider provider
    );

    boolean existsByAccountId(String accountId);
}
