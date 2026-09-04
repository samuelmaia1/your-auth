package com.samuelmaia1_github.yourauth.infra.repository;

import com.samuelmaia1_github.yourauth.infra.repository.entity.AccountSubscriptionEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountSubscriptionEventJpaRepository extends JpaRepository<AccountSubscriptionEventEntity, String> {
}
