package com.samuelmaia1_github.yourauth.infra.repository;

import com.samuelmaia1_github.yourauth.infra.repository.entity.AccountSubscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AccountSubscriptionJpaRepository extends JpaRepository<AccountSubscriptionEntity, String> {
    @Query("""
            select subscription
            from AccountSubscriptionEntity subscription
            where exists (
                select 1
                from AccountCurrentSubscriptionEntity currentSubscription
                where currentSubscription.accountId = :accountId
                  and currentSubscription.subscriptionId = subscription.id
            )
            """)
    Optional<AccountSubscriptionEntity> findCurrentByAccountId(@Param("accountId") String accountId);
}
