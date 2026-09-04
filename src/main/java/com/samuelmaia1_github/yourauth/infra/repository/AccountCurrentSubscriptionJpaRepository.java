package com.samuelmaia1_github.yourauth.infra.repository;

import com.samuelmaia1_github.yourauth.infra.repository.entity.AccountCurrentSubscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountCurrentSubscriptionJpaRepository extends JpaRepository<AccountCurrentSubscriptionEntity, String> {
}
