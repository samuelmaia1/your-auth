package com.samuelmaia1_github.yourauth.presentation.dto.subscription;

import com.samuelmaia1_github.yourauth.domain.subscription.BillingCycle;
import com.samuelmaia1_github.yourauth.domain.subscription.SubscriptionStatus;
import com.samuelmaia1_github.yourauth.presentation.dto.plan.PlanResponseDTO;

import java.time.LocalDateTime;

public record AccountSubscriptionResponseDTO(
        String id,
        String accountId,
        PlanResponseDTO plan,
        SubscriptionStatus status,
        BillingCycle billingCycle,
        LocalDateTime currentPeriodStart,
        LocalDateTime currentPeriodEnd,
        LocalDateTime trialEndsAt,
        LocalDateTime canceledAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
