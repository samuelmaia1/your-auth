package com.samuelmaia1_github.yourauth.presentation.dto.plan;

import com.samuelmaia1_github.yourauth.domain.plan.PlanCode;

import java.util.List;

public record PlanResponseDTO(
        String id,
        PlanCode code,
        String name,
        String description,
        boolean active,
        int displayOrder,
        List<PlanFeatureResponseDTO> features,
        List<PlanLimitResponseDTO> limits
) {
}
