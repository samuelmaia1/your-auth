package com.samuelmaia1_github.yourauth.presentation.mapper;

import com.samuelmaia1_github.yourauth.domain.account.AccountUsage;
import com.samuelmaia1_github.yourauth.domain.plan.Plan;
import com.samuelmaia1_github.yourauth.domain.plan.PlanLimitCode;
import com.samuelmaia1_github.yourauth.domain.subscription.AccountSubscription;
import com.samuelmaia1_github.yourauth.presentation.dto.account.AccountUsageLimitsResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.account.AccountUsageResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.account.AccountUsageTotalsResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class AccountUsagePresentationMapper {
    public static AccountUsageResponseDTO toResponseDTO(AccountUsage usage, AccountSubscription subscription) {
        Plan plan = subscription.getPlan();

        return new AccountUsageResponseDTO(
                plan.getCode(),
                subscription.getStatus(),
                new AccountUsageTotalsResponseDTO(
                        usage.totalProjects(),
                        usage.totalUsers(),
                        usage.totalActiveSessions()
                ),
                new AccountUsageLimitsResponseDTO(
                        plan.limitValue(PlanLimitCode.MAX_PROJECTS),
                        plan.limitValue(PlanLimitCode.MAX_USERS_TOTAL),
                        plan.limitValue(PlanLimitCode.MAX_ACTIVE_SESSIONS_TOTAL)
                )
        );
    }
}
