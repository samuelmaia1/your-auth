package com.samuelmaia1_github.yourauth.presentation.dto.plan;

public record PlanFeatureResponseDTO(
        String id,
        String code,
        String description,
        boolean enabled
) {
}
