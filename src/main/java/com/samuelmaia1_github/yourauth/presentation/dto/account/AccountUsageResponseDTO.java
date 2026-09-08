package com.samuelmaia1_github.yourauth.presentation.dto.account;

import com.samuelmaia1_github.yourauth.domain.plan.PlanCode;
import com.samuelmaia1_github.yourauth.domain.subscription.SubscriptionStatus;

public record AccountUsageResponseDTO(
        PlanCode planCode,
        SubscriptionStatus subscriptionStatus,
        AccountUsageTotalsResponseDTO usage,
        AccountUsageLimitsResponseDTO limits
) {
}
