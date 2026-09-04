package com.samuelmaia1_github.yourauth.presentation.mapper;

import com.samuelmaia1_github.yourauth.domain.plan.Plan;
import com.samuelmaia1_github.yourauth.domain.plan.PlanFeature;
import com.samuelmaia1_github.yourauth.domain.plan.PlanLimit;
import com.samuelmaia1_github.yourauth.presentation.dto.plan.PlanFeatureResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.plan.PlanLimitResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.plan.PlanResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PlanPresentationMapper {
    public static PlanResponseDTO toResponseDTO(Plan plan) {
        if (plan == null) {
            return null;
        }

        return new PlanResponseDTO(
                plan.getId(),
                plan.getCode(),
                plan.getName(),
                plan.getDescription(),
                plan.isActive(),
                plan.getDisplayOrder(),
                toFeatureResponseDTO(plan.getFeatures()),
                toLimitResponseDTO(plan.getLimits())
        );
    }

    public static List<PlanResponseDTO> toResponseDTO(List<Plan> plans) {
        return plans.stream()
                .map(PlanPresentationMapper::toResponseDTO)
                .toList();
    }

    private static List<PlanFeatureResponseDTO> toFeatureResponseDTO(List<PlanFeature> features) {
        return features.stream()
                .map(feature -> new PlanFeatureResponseDTO(
                        feature.getId(),
                        feature.getCode(),
                        feature.getDescription(),
                        feature.isEnabled()
                ))
                .toList();
    }

    private static List<PlanLimitResponseDTO> toLimitResponseDTO(List<PlanLimit> limits) {
        return limits.stream()
                .map(limit -> new PlanLimitResponseDTO(
                        limit.getId(),
                        limit.getCode(),
                        limit.getValue(),
                        limit.getUnit(),
                        limit.getPeriod(),
                        limit.isUnlimited()
                ))
                .toList();
    }
}
