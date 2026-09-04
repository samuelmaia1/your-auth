package com.samuelmaia1_github.yourauth.domain.subscription;

import com.samuelmaia1_github.yourauth.domain.plan.Plan;
import com.samuelmaia1_github.yourauth.domain.plan.PlanCode;
import com.samuelmaia1_github.yourauth.domain.plan.PlanRepository;
import com.samuelmaia1_github.yourauth.domain.plan.exceptions.PlanNotFoundException;
import com.samuelmaia1_github.yourauth.domain.subscription.exceptions.AccountSubscriptionNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AccountSubscriptionService {
    private final AccountSubscriptionRepository subscriptionRepository;
    private final AccountSubscriptionEventRepository eventRepository;
    private final PlanRepository planRepository;

    @Transactional
    public AccountSubscription createFreeSubscription(String accountId) {
        return subscriptionRepository.findCurrentByAccountId(accountId)
                .map(this::attachPlan)
                .orElseGet(() -> createSubscription(
                        accountId,
                        PlanCode.FREE,
                        BillingCycle.NONE,
                        null,
                        AccountSubscriptionEventType.CREATED,
                        null
                ));
    }

    public AccountSubscription findCurrentByAccountId(String accountId) {
        AccountSubscription subscription = subscriptionRepository.findCurrentByAccountId(accountId)
                .orElseThrow(AccountSubscriptionNotFoundException::new);

        return attachPlan(subscription);
    }

    @Transactional
    public AccountSubscription changePlan(
            String accountId,
            PlanCode planCode,
            BillingCycle billingCycle,
            String changedByAccountId
    ) {
        AccountSubscription currentSubscription = subscriptionRepository.findCurrentByAccountId(accountId)
                .map(this::attachPlan)
                .orElse(null);

        if (
                currentSubscription != null
                        && currentSubscription.getPlan() != null
                        && currentSubscription.getPlan().getCode() == planCode
                        && SubscriptionStatus.ACTIVE.equals(currentSubscription.getStatus())
        ) {
            return currentSubscription;
        }

        if (currentSubscription != null) {
            currentSubscription.cancel(LocalDateTime.now());
            subscriptionRepository.save(currentSubscription);
        }

        return createSubscription(
                accountId,
                planCode,
                resolveBillingCycle(planCode, billingCycle),
                changedByAccountId,
                currentSubscription == null ? AccountSubscriptionEventType.CREATED : AccountSubscriptionEventType.PLAN_CHANGED,
                currentSubscription == null ? null : currentSubscription.getPlanId()
        );
    }

    private AccountSubscription createSubscription(
            String accountId,
            PlanCode planCode,
            BillingCycle billingCycle,
            String createdByAccountId,
            AccountSubscriptionEventType eventType,
            String previousPlanId
    ) {
        Plan plan = findPlanOrThrow(planCode);
        AccountSubscription subscription = AccountSubscription.builder()
                .accountId(accountId)
                .planId(plan.getId())
                .status(SubscriptionStatus.ACTIVE)
                .billingCycle(billingCycle)
                .currentPeriodStart(LocalDateTime.now())
                .build();

        AccountSubscription savedSubscription = subscriptionRepository.save(subscription);
        subscriptionRepository.saveCurrent(accountId, savedSubscription.getId());
        eventRepository.save(AccountSubscriptionEvent.builder()
                .accountId(accountId)
                .subscriptionId(savedSubscription.getId())
                .eventType(eventType)
                .previousPlanId(previousPlanId)
                .newPlanId(plan.getId())
                .occurredAt(LocalDateTime.now())
                .description(descriptionFor(eventType, plan))
                .createdByAccountId(createdByAccountId)
                .build());

        return savedSubscription.withPlan(plan);
    }

    private AccountSubscription attachPlan(AccountSubscription subscription) {
        Plan plan = planRepository.findById(subscription.getPlanId())
                .orElseThrow(PlanNotFoundException::new);

        return subscription.withPlan(plan);
    }

    private Plan findPlanOrThrow(PlanCode code) {
        return planRepository.findByCode(code)
                .orElseThrow(PlanNotFoundException::new);
    }

    private String descriptionFor(AccountSubscriptionEventType eventType, Plan plan) {
        if (AccountSubscriptionEventType.PLAN_CHANGED.equals(eventType)) {
            return "Plano da assinatura alterado para " + plan.getCode() + ".";
        }

        return "Assinatura criada no plano " + plan.getCode() + ".";
    }

    private BillingCycle resolveBillingCycle(PlanCode planCode, BillingCycle requestedBillingCycle) {
        if (PlanCode.FREE.equals(planCode)) {
            return BillingCycle.NONE;
        }

        if (requestedBillingCycle == null || BillingCycle.NONE.equals(requestedBillingCycle)) {
            return BillingCycle.MONTHLY;
        }

        return requestedBillingCycle;
    }
}
