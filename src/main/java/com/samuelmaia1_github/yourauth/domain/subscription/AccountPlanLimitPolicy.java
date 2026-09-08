package com.samuelmaia1_github.yourauth.domain.subscription;

import com.samuelmaia1_github.yourauth.domain.account.AccountUsage;
import com.samuelmaia1_github.yourauth.domain.account.AccountUsageRepository;
import com.samuelmaia1_github.yourauth.domain.plan.Plan;
import com.samuelmaia1_github.yourauth.domain.plan.PlanLimit;
import com.samuelmaia1_github.yourauth.domain.plan.PlanLimitCode;
import com.samuelmaia1_github.yourauth.domain.plan.PlanRepository;
import com.samuelmaia1_github.yourauth.domain.plan.exceptions.PlanLimitExceededException;
import com.samuelmaia1_github.yourauth.domain.plan.exceptions.PlanLimitNotConfiguredException;
import com.samuelmaia1_github.yourauth.domain.plan.exceptions.PlanNotFoundException;
import com.samuelmaia1_github.yourauth.domain.subscription.exceptions.AccountSubscriptionNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountPlanLimitPolicy {
    private final AccountSubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;
    private final AccountUsageRepository usageRepository;

    public void ensureCanCreateProject(String accountId) {
        AccountPlanLimitContext context = loadContext(accountId);

        ensureLimitIsNotExceeded(
                context.plan(),
                PlanLimitCode.MAX_PROJECTS,
                context.usage().totalProjects(),
                "Limite de projetos do plano atual excedido."
        );
    }

    public void ensureCanCreateUser(String accountId) {
        AccountPlanLimitContext context = loadContext(accountId);

        ensureLimitIsNotExceeded(
                context.plan(),
                PlanLimitCode.MAX_USERS_TOTAL,
                context.usage().totalUsers(),
                "Limite de usuários do plano atual excedido."
        );
    }

    public void ensureCanCreateSession(String accountId) {
        AccountPlanLimitContext context = loadContext(accountId);

        ensureLimitIsNotExceeded(
                context.plan(),
                PlanLimitCode.MAX_ACTIVE_SESSIONS_TOTAL,
                context.usage().totalActiveSessions(),
                "Limite de sessões ativas do plano atual excedido."
        );
    }

    private AccountPlanLimitContext loadContext(String accountId) {
        AccountSubscription subscription = subscriptionRepository.findCurrentByAccountId(accountId)
                .orElseThrow(AccountSubscriptionNotFoundException::new);
        Plan plan = planRepository.findById(subscription.getPlanId())
                .orElseThrow(PlanNotFoundException::new);
        AccountUsage usage = usageRepository.findByOwnerAccountId(accountId);

        return new AccountPlanLimitContext(plan, usage);
    }

    private void ensureLimitIsNotExceeded(
            Plan plan,
            PlanLimitCode code,
            long currentUsage,
            String exceededMessage
    ) {
        PlanLimit limit = plan.findLimit(code)
                .orElseThrow(() -> new PlanLimitNotConfiguredException(
                        "Limite " + code.name() + " não configurado para o plano atual."
                ));

        if (limit.isUnlimited()) {
            return;
        }

        if (currentUsage >= limit.getValue()) {
            throw new PlanLimitExceededException(exceededMessage);
        }
    }

    private record AccountPlanLimitContext(Plan plan, AccountUsage usage) {
    }
}
