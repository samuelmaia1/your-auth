package com.samuelmaia1_github.yourauth.domain.subscription;

import com.samuelmaia1_github.yourauth.domain.plan.Plan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class AccountSubscription {
    private String id;
    private String accountId;
    private String planId;
    private Plan plan;
    private SubscriptionStatus status;
    private BillingCycle billingCycle;
    private LocalDateTime currentPeriodStart;
    private LocalDateTime currentPeriodEnd;
    private LocalDateTime trialEndsAt;
    private LocalDateTime canceledAt;
    private String externalCustomerId;
    private String externalSubscriptionId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AccountSubscription withPlan(Plan plan) {
        return toBuilder()
                .plan(plan)
                .build();
    }

    public void cancel(LocalDateTime canceledAt) {
        this.status = SubscriptionStatus.CANCELED;
        this.canceledAt = canceledAt;
    }
}
