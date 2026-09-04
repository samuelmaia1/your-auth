package com.samuelmaia1_github.yourauth.infra.repository.adapter;

import com.samuelmaia1_github.yourauth.domain.subscription.AccountSubscription;
import com.samuelmaia1_github.yourauth.domain.subscription.AccountSubscriptionRepository;
import com.samuelmaia1_github.yourauth.infra.mappers.AccountSubscriptionMapper;
import com.samuelmaia1_github.yourauth.infra.repository.AccountCurrentSubscriptionJpaRepository;
import com.samuelmaia1_github.yourauth.infra.repository.AccountSubscriptionJpaRepository;
import com.samuelmaia1_github.yourauth.infra.repository.entity.AccountCurrentSubscriptionEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AccountSubscriptionRepositoryAdapter implements AccountSubscriptionRepository {
    private final AccountSubscriptionJpaRepository subscriptionRepository;
    private final AccountCurrentSubscriptionJpaRepository currentSubscriptionRepository;

    @Override
    public AccountSubscription save(AccountSubscription subscription) {
        return AccountSubscriptionMapper.toDomain(
                subscriptionRepository.save(AccountSubscriptionMapper.toEntity(subscription))
        );
    }

    @Override
    public Optional<AccountSubscription> findById(String id) {
        return subscriptionRepository.findById(id)
                .map(AccountSubscriptionMapper::toDomain);
    }

    @Override
    public Optional<AccountSubscription> findCurrentByAccountId(String accountId) {
        return subscriptionRepository.findCurrentByAccountId(accountId)
                .map(AccountSubscriptionMapper::toDomain);
    }

    @Override
    public void saveCurrent(String accountId, String subscriptionId) {
        AccountCurrentSubscriptionEntity currentSubscription = currentSubscriptionRepository.findById(accountId)
                .orElseGet(() -> AccountCurrentSubscriptionEntity.builder()
                        .accountId(accountId)
                        .build());

        currentSubscription.setSubscriptionId(subscriptionId);
        currentSubscriptionRepository.save(currentSubscription);
    }
}
