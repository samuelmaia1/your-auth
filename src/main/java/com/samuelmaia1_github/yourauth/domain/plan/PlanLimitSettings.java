package com.samuelmaia1_github.yourauth.domain.plan;

import java.util.ArrayList;
import java.util.List;

public record PlanLimitSettings(
        Long maxProjects,
        boolean maxProjectsProvided,
        Long maxUsersTotal,
        boolean maxUsersTotalProvided,
        Long maxActiveSessionsTotal,
        boolean maxActiveSessionsTotalProvided
) {
    public PlanLimitSettings(Long maxProjects, Long maxUsersTotal, Long maxActiveSessionsTotal) {
        this(
                maxProjects,
                true,
                maxUsersTotal,
                true,
                maxActiveSessionsTotal,
                true
        );
    }

    public List<PlanLimit> toLimits(String planId) {
        List<PlanLimit> limits = new ArrayList<>();

        addLimit(limits, planId, PlanLimitCode.MAX_PROJECTS, maxProjects, maxProjectsProvided);
        addLimit(limits, planId, PlanLimitCode.MAX_USERS_TOTAL, maxUsersTotal, maxUsersTotalProvided);
        addLimit(limits, planId, PlanLimitCode.MAX_ACTIVE_SESSIONS_TOTAL, maxActiveSessionsTotal, maxActiveSessionsTotalProvided);

        return List.copyOf(limits);
    }

    public boolean hasChanges() {
        return maxProjectsProvided
                || maxUsersTotalProvided
                || maxActiveSessionsTotalProvided;
    }

    private static void addLimit(
            List<PlanLimit> limits,
            String planId,
            PlanLimitCode code,
            Long value,
            boolean provided
    ) {
        if (provided) {
            limits.add(PlanLimit.countLimit(planId, code, value));
        }
    }
}
