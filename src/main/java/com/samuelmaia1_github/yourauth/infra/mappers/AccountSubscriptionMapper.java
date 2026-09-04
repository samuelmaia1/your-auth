package com.samuelmaia1_github.yourauth.infra.mappers;

import com.samuelmaia1_github.yourauth.domain.subscription.AccountSubscription;
import com.samuelmaia1_github.yourauth.infra.repository.entity.AccountSubscriptionEntity;

public class AccountSubscriptionMapper {
    private AccountSubscriptionMapper() {
    }

    public static AccountSubscription toDomain(AccountSubscriptionEntity entity) {
        if (entity == null) {
            return null;
        }

        return AccountSubscription.builder()
                .id(entity.getId())
                .accountId(entity.getAccountId())
                .planId(entity.getPlanId())
                .status(entity.getStatus())
                .billingCycle(entity.getBillingCycle())
                .currentPeriodStart(entity.getCurrentPeriodStart())
                .currentPeriodEnd(entity.getCurrentPeriodEnd())
                .trialEndsAt(entity.getTrialEndsAt())
                .canceledAt(entity.getCanceledAt())
                .externalCustomerId(entity.getExternalCustomerId())
                .externalSubscriptionId(entity.getExternalSubscriptionId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static AccountSubscriptionEntity toEntity(AccountSubscription subscription) {
        if (subscription == null) {
            return null;
        }

        return AccountSubscriptionEntity.builder()
                .id(subscription.getId())
                .accountId(subscription.getAccountId())
                .planId(subscription.getPlanId())
                .status(subscription.getStatus())
                .billingCycle(subscription.getBillingCycle())
                .currentPeriodStart(subscription.getCurrentPeriodStart())
                .currentPeriodEnd(subscription.getCurrentPeriodEnd())
                .trialEndsAt(subscription.getTrialEndsAt())
                .canceledAt(subscription.getCanceledAt())
                .externalCustomerId(subscription.getExternalCustomerId())
                .externalSubscriptionId(subscription.getExternalSubscriptionId())
                .createdAt(subscription.getCreatedAt())
                .updatedAt(subscription.getUpdatedAt())
                .build();
    }
}
