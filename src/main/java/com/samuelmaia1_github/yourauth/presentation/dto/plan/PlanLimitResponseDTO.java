package com.samuelmaia1_github.yourauth.presentation.dto.plan;

import com.samuelmaia1_github.yourauth.domain.plan.PlanLimitPeriod;

public record PlanLimitResponseDTO(
        String id,
        String code,
        Long value,
        String unit,
        PlanLimitPeriod period,
        boolean unlimited
) {
}
