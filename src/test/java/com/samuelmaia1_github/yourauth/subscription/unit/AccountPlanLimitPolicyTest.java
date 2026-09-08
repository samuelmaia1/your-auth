package com.samuelmaia1_github.yourauth.subscription.unit;

import com.samuelmaia1_github.yourauth.domain.account.AccountUsage;
import com.samuelmaia1_github.yourauth.domain.account.AccountUsageRepository;
import com.samuelmaia1_github.yourauth.domain.plan.Plan;
import com.samuelmaia1_github.yourauth.domain.plan.PlanCode;
import com.samuelmaia1_github.yourauth.domain.plan.PlanLimit;
import com.samuelmaia1_github.yourauth.domain.plan.PlanLimitCode;
import com.samuelmaia1_github.yourauth.domain.plan.PlanRepository;
import com.samuelmaia1_github.yourauth.domain.plan.exceptions.PlanLimitExceededException;
import com.samuelmaia1_github.yourauth.domain.subscription.AccountPlanLimitPolicy;
import com.samuelmaia1_github.yourauth.domain.subscription.AccountSubscription;
import com.samuelmaia1_github.yourauth.domain.subscription.AccountSubscriptionRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountPlanLimitPolicyTest {
    @Test
    void shouldDenyProjectCreationWhenProjectLimitIsReached() {
        AccountPlanLimitPolicy policy = policyFor(new AccountUsage(1L, 0L, 0L), planWithLimits(1L, 100L, 200L));

        assertThatThrownBy(() -> policy.ensureCanCreateProject("account-id"))
                .isInstanceOf(PlanLimitExceededException.class)
                .hasMessage("Limite de projetos do plano atual excedido.");
    }

    @Test
    void shouldAllowUserCreationWhenUserLimitIsNotReached() {
        AccountPlanLimitPolicy policy = policyFor(new AccountUsage(1L, 99L, 0L), planWithLimits(1L, 100L, 200L));

        assertThatCode(() -> policy.ensureCanCreateUser("account-id"))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldAllowSessionCreationWhenLimitIsUnlimited() {
        AccountPlanLimitPolicy policy = policyFor(new AccountUsage(1L, 100L, 1000L), planWithLimits(1L, 100L, null));

        assertThatCode(() -> policy.ensureCanCreateSession("account-id"))
                .doesNotThrowAnyException();
    }

    private static AccountPlanLimitPolicy policyFor(AccountUsage usage, Plan plan) {
        return new AccountPlanLimitPolicy(
                new StubAccountSubscriptionRepository(),
                new StubPlanRepository(plan),
                new StubAccountUsageRepository(usage)
        );
    }

    private static Plan planWithLimits(Long maxProjects, Long maxUsersTotal, Long maxActiveSessionsTotal) {
        return Plan.builder()
                .id("free")
                .code(PlanCode.FREE)
                .name("Free")
                .active(true)
                .limits(List.of(
                        PlanLimit.countLimit("free", PlanLimitCode.MAX_PROJECTS, maxProjects),
                        PlanLimit.countLimit("free", PlanLimitCode.MAX_USERS_TOTAL, maxUsersTotal),
                        PlanLimit.countLimit("free", PlanLimitCode.MAX_ACTIVE_SESSIONS_TOTAL, maxActiveSessionsTotal)
                ))
                .build();
    }

    private static class StubAccountSubscriptionRepository implements AccountSubscriptionRepository {
        @Override
        public AccountSubscription save(AccountSubscription subscription) {
            return subscription;
        }

        @Override
        public Optional<AccountSubscription> findById(String id) {
            return Optional.empty();
        }

        @Override
        public Optional<AccountSubscription> findCurrentByAccountId(String accountId) {
            return Optional.of(AccountSubscription.builder()
                    .accountId(accountId)
                    .planId("free")
                    .build());
        }

        @Override
        public void saveCurrent(String accountId, String subscriptionId) {
        }
    }

    private static class StubPlanRepository implements PlanRepository {
        private final Plan plan;

        private StubPlanRepository(Plan plan) {
            this.plan = plan;
        }

        @Override
        public List<Plan> findAllActive() {
            return List.of(plan);
        }

        @Override
        public Optional<Plan> findById(String id) {
            return Optional.of(plan);
        }

        @Override
        public Optional<Plan> findByCode(PlanCode code) {
            return Optional.of(plan);
        }

        @Override
        public List<PlanLimit> saveLimits(String planId, List<PlanLimit> limits) {
            return limits;
        }
    }

    private static class StubAccountUsageRepository implements AccountUsageRepository {
        private final AccountUsage usage;

        private StubAccountUsageRepository(AccountUsage usage) {
            this.usage = usage;
        }

        @Override
        public AccountUsage findByOwnerAccountId(String accountId) {
            return usage;
        }
    }
}
