package com.samuelmaia1_github.yourauth.infra.mappers;

import com.samuelmaia1_github.yourauth.domain.subscription.AccountSubscriptionEvent;
import com.samuelmaia1_github.yourauth.infra.repository.entity.AccountSubscriptionEventEntity;

public class AccountSubscriptionEventMapper {
    private AccountSubscriptionEventMapper() {
    }

    public static AccountSubscriptionEvent toDomain(AccountSubscriptionEventEntity entity) {
        if (entity == null) {
            return null;
        }

        return AccountSubscriptionEvent.builder()
                .id(entity.getId())
                .accountId(entity.getAccountId())
                .subscriptionId(entity.getSubscriptionId())
                .eventType(entity.getEventType())
                .previousPlanId(entity.getPreviousPlanId())
                .newPlanId(entity.getNewPlanId())
                .occurredAt(entity.getOccurredAt())
                .description(entity.getDescription())
                .createdByAccountId(entity.getCreatedByAccountId())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static AccountSubscriptionEventEntity toEntity(AccountSubscriptionEvent event) {
        if (event == null) {
            return null;
        }

        return AccountSubscriptionEventEntity.builder()
                .id(event.getId())
                .accountId(event.getAccountId())
                .subscriptionId(event.getSubscriptionId())
                .eventType(event.getEventType())
                .previousPlanId(event.getPreviousPlanId())
                .newPlanId(event.getNewPlanId())
                .occurredAt(event.getOccurredAt())
                .description(event.getDescription())
                .createdByAccountId(event.getCreatedByAccountId())
                .createdAt(event.getCreatedAt())
                .build();
    }
}
