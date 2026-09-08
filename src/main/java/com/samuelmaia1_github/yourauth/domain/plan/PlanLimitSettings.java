package com.samuelmaia1_github.yourauth.domain.plan;

import java.util.List;

public record PlanLimitSettings(
        Long maxProjects,
        Long maxUsersTotal,
        Long maxActiveSessionsTotal
) {
    public List<PlanLimit> toLimits(String planId) {
        return List.of(
                PlanLimit.countLimit(planId, PlanLimitCode.MAX_PROJECTS, maxProjects),
                PlanLimit.countLimit(planId, PlanLimitCode.MAX_USERS_TOTAL, maxUsersTotal),
                PlanLimit.countLimit(planId, PlanLimitCode.MAX_ACTIVE_SESSIONS_TOTAL, maxActiveSessionsTotal)
        );
    }
}
