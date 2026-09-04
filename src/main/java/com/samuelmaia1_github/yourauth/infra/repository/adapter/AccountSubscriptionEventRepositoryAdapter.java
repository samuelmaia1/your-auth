package com.samuelmaia1_github.yourauth.infra.repository.adapter;

import com.samuelmaia1_github.yourauth.domain.subscription.AccountSubscriptionEvent;
import com.samuelmaia1_github.yourauth.domain.subscription.AccountSubscriptionEventRepository;
import com.samuelmaia1_github.yourauth.infra.mappers.AccountSubscriptionEventMapper;
import com.samuelmaia1_github.yourauth.infra.repository.AccountSubscriptionEventJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountSubscriptionEventRepositoryAdapter implements AccountSubscriptionEventRepository {
    private final AccountSubscriptionEventJpaRepository repository;

    @Override
    public AccountSubscriptionEvent save(AccountSubscriptionEvent event) {
        return AccountSubscriptionEventMapper.toDomain(
                repository.save(AccountSubscriptionEventMapper.toEntity(event))
        );
    }
}
