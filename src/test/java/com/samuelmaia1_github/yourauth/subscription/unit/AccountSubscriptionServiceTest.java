package com.samuelmaia1_github.yourauth.subscription.unit;

import com.samuelmaia1_github.yourauth.domain.plan.Plan;
import com.samuelmaia1_github.yourauth.domain.plan.PlanCode;
import com.samuelmaia1_github.yourauth.domain.plan.PlanRepository;
import com.samuelmaia1_github.yourauth.domain.subscription.AccountSubscription;
import com.samuelmaia1_github.yourauth.domain.subscription.AccountSubscriptionEvent;
import com.samuelmaia1_github.yourauth.domain.subscription.AccountSubscriptionEventRepository;
import com.samuelmaia1_github.yourauth.domain.subscription.AccountSubscriptionEventType;
import com.samuelmaia1_github.yourauth.domain.subscription.AccountSubscriptionRepository;
import com.samuelmaia1_github.yourauth.domain.subscription.AccountSubscriptionService;
import com.samuelmaia1_github.yourauth.domain.subscription.BillingCycle;
import com.samuelmaia1_github.yourauth.domain.subscription.SubscriptionStatus;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AccountSubscriptionServiceTest {
    @Test
    void shouldCreateFreeSubscriptionForAccountWithoutCurrentSubscription() {
        RecordingAccountSubscriptionRepository subscriptionRepository = new RecordingAccountSubscriptionRepository();
        RecordingAccountSubscriptionEventRepository eventRepository = new RecordingAccountSubscriptionEventRepository();
        AccountSubscriptionService service = new AccountSubscriptionService(
                subscriptionRepository,
                eventRepository,
                new StubPlanRepository()
        );

        AccountSubscription subscription = service.createFreeSubscription("account-id");

        assertThat(subscription.getAccountId()).isEqualTo("account-id");
        assertThat(subscription.getPlan().getCode()).isEqualTo(PlanCode.FREE);
        assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(subscription.getBillingCycle()).isEqualTo(BillingCycle.NONE);
        assertThat(subscriptionRepository.currentAccountId).isEqualTo("account-id");
        assertThat(subscriptionRepository.currentSubscriptionId).isEqualTo(subscription.getId());
        assertThat(eventRepository.savedEvents).hasSize(1);
        assertThat(eventRepository.savedEvents.getFirst().getEventType())
                .isEqualTo(AccountSubscriptionEventType.CREATED);
        assertThat(eventRepository.savedEvents.getFirst().getNewPlanId()).isEqualTo("free");
    }

    @Test
    void shouldReturnExistingCurrentSubscriptionWhenCreatingFreeSubscriptionAgain() {
        RecordingAccountSubscriptionRepository subscriptionRepository = new RecordingAccountSubscriptionRepository();
        AccountSubscription currentSubscription = AccountSubscription.builder()
                .id("current-subscription-id")
                .accountId("account-id")
                .planId("starter")
                .status(SubscriptionStatus.ACTIVE)
                .billingCycle(BillingCycle.MONTHLY)
                .build();
        subscriptionRepository.currentSubscription = currentSubscription;
        RecordingAccountSubscriptionEventRepository eventRepository = new RecordingAccountSubscriptionEventRepository();
        AccountSubscriptionService service = new AccountSubscriptionService(
                subscriptionRepository,
                eventRepository,
                new StubPlanRepository()
        );

        AccountSubscription subscription = service.createFreeSubscription("account-id");

        assertThat(subscription.getId()).isEqualTo("current-subscription-id");
        assertThat(subscription.getPlan().getCode()).isEqualTo(PlanCode.STARTER);
        assertThat(subscriptionRepository.savedSubscriptions).isEmpty();
        assertThat(eventRepository.savedEvents).isEmpty();
    }

    @Test
    void shouldChangeCurrentPlanAndCancelPreviousSubscription() {
        RecordingAccountSubscriptionRepository subscriptionRepository = new RecordingAccountSubscriptionRepository();
        AccountSubscription currentSubscription = AccountSubscription.builder()
                .id("current-subscription-id")
                .accountId("account-id")
                .planId("free")
                .status(SubscriptionStatus.ACTIVE)
                .billingCycle(BillingCycle.NONE)
                .build();
        subscriptionRepository.currentSubscription = currentSubscription;
        RecordingAccountSubscriptionEventRepository eventRepository = new RecordingAccountSubscriptionEventRepository();
        AccountSubscriptionService service = new AccountSubscriptionService(
                subscriptionRepository,
                eventRepository,
                new StubPlanRepository()
        );

        AccountSubscription subscription = service.changePlan(
                "account-id",
                PlanCode.PRO,
                null,
                "admin-account-id"
        );

        assertThat(subscription.getPlan().getCode()).isEqualTo(PlanCode.PRO);
        assertThat(subscription.getBillingCycle()).isEqualTo(BillingCycle.MONTHLY);
        assertThat(subscriptionRepository.savedSubscriptions).hasSize(2);
        assertThat(subscriptionRepository.savedSubscriptions.getFirst().getStatus())
                .isEqualTo(SubscriptionStatus.CANCELED);
        assertThat(subscriptionRepository.currentSubscriptionId).isEqualTo(subscription.getId());
        assertThat(eventRepository.savedEvents).hasSize(1);
        assertThat(eventRepository.savedEvents.getFirst().getEventType())
                .isEqualTo(AccountSubscriptionEventType.PLAN_CHANGED);
        assertThat(eventRepository.savedEvents.getFirst().getPreviousPlanId()).isEqualTo("free");
        assertThat(eventRepository.savedEvents.getFirst().getNewPlanId()).isEqualTo("pro");
        assertThat(eventRepository.savedEvents.getFirst().getCreatedByAccountId()).isEqualTo("admin-account-id");
    }

    private static class StubPlanRepository implements PlanRepository {
        private final List<Plan> plans = List.of(
                plan("free", PlanCode.FREE),
                plan("starter", PlanCode.STARTER),
                plan("pro", PlanCode.PRO),
                plan("business", PlanCode.BUSINESS)
        );

        @Override
        public List<Plan> findAllActive() {
            return plans;
        }

        @Override
        public Optional<Plan> findById(String id) {
            return plans.stream()
                    .filter(plan -> plan.getId().equals(id))
                    .findFirst();
        }

        @Override
        public Optional<Plan> findByCode(PlanCode code) {
            return plans.stream()
                    .filter(plan -> plan.getCode() == code)
                    .findFirst();
        }

        private static Plan plan(String id, PlanCode code) {
            return Plan.builder()
                    .id(id)
                    .code(code)
                    .name(code.name())
                    .active(true)
                    .build();
        }
    }

    private static class RecordingAccountSubscriptionRepository implements AccountSubscriptionRepository {
        private final List<AccountSubscription> savedSubscriptions = new ArrayList<>();
        private AccountSubscription currentSubscription;
        private String currentAccountId;
        private String currentSubscriptionId;

        @Override
        public AccountSubscription save(AccountSubscription subscription) {
            if (subscription.getId() == null) {
                subscription = subscription.toBuilder()
                        .id("subscription-" + (savedSubscriptions.size() + 1))
                        .build();
            }

            savedSubscriptions.add(subscription);
            return subscription;
        }

        @Override
        public Optional<AccountSubscription> findById(String id) {
            return savedSubscriptions.stream()
                    .filter(subscription -> subscription.getId().equals(id))
                    .findFirst();
        }

        @Override
        public Optional<AccountSubscription> findCurrentByAccountId(String accountId) {
            return Optional.ofNullable(currentSubscription);
        }

        @Override
        public void saveCurrent(String accountId, String subscriptionId) {
            currentAccountId = accountId;
            currentSubscriptionId = subscriptionId;
        }
    }

    private static class RecordingAccountSubscriptionEventRepository implements AccountSubscriptionEventRepository {
        private final List<AccountSubscriptionEvent> savedEvents = new ArrayList<>();

        @Override
        public AccountSubscriptionEvent save(AccountSubscriptionEvent event) {
            savedEvents.add(event);
            return event;
        }
    }
}
