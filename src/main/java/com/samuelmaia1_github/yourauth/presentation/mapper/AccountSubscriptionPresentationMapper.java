package com.samuelmaia1_github.yourauth.presentation.mapper;

import com.samuelmaia1_github.yourauth.domain.subscription.AccountSubscription;
import com.samuelmaia1_github.yourauth.presentation.dto.subscription.AccountSubscriptionResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class AccountSubscriptionPresentationMapper {
    public static AccountSubscriptionResponseDTO toResponseDTO(AccountSubscription subscription) {
        return new AccountSubscriptionResponseDTO(
                subscription.getId(),
                subscription.getAccountId(),
                PlanPresentationMapper.toResponseDTO(subscription.getPlan()),
                subscription.getStatus(),
                subscription.getBillingCycle(),
                subscription.getCurrentPeriodStart(),
                subscription.getCurrentPeriodEnd(),
                subscription.getTrialEndsAt(),
                subscription.getCanceledAt(),
                subscription.getCreatedAt(),
                subscription.getUpdatedAt()
        );
    }
}
