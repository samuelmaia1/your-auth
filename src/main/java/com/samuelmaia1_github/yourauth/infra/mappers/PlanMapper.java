package com.samuelmaia1_github.yourauth.infra.mappers;

import com.samuelmaia1_github.yourauth.domain.plan.Plan;
import com.samuelmaia1_github.yourauth.domain.plan.PlanFeature;
import com.samuelmaia1_github.yourauth.domain.plan.PlanLimit;
import com.samuelmaia1_github.yourauth.infra.repository.entity.PlanEntity;
import com.samuelmaia1_github.yourauth.infra.repository.entity.PlanFeatureEntity;
import com.samuelmaia1_github.yourauth.infra.repository.entity.PlanLimitEntity;

import java.util.List;

public class PlanMapper {
    private PlanMapper() {
    }

    public static Plan toDomain(
            PlanEntity entity,
            List<PlanFeatureEntity> featureEntities,
            List<PlanLimitEntity> limitEntities
    ) {
        if (entity == null) {
            return null;
        }

        return Plan.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .active(entity.isActive())
                .displayOrder(entity.getDisplayOrder())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .features(toFeatureDomains(featureEntities))
                .limits(toLimitDomains(limitEntities))
                .build();
    }

    private static List<PlanFeature> toFeatureDomains(List<PlanFeatureEntity> entities) {
        if (entities == null) {
            return List.of();
        }

        return entities.stream()
                .map(PlanMapper::toFeatureDomain)
                .toList();
    }

    private static PlanFeature toFeatureDomain(PlanFeatureEntity entity) {
        return PlanFeature.builder()
                .id(entity.getId())
                .planId(entity.getPlanId())
                .code(entity.getCode())
                .description(entity.getDescription())
                .enabled(entity.isEnabled())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private static List<PlanLimit> toLimitDomains(List<PlanLimitEntity> entities) {
        if (entities == null) {
            return List.of();
        }

        return entities.stream()
                .map(PlanMapper::toLimitDomain)
                .toList();
    }

    private static PlanLimit toLimitDomain(PlanLimitEntity entity) {
        return PlanLimit.builder()
                .id(entity.getId())
                .planId(entity.getPlanId())
                .code(entity.getCode())
                .value(entity.getValue())
                .unit(entity.getUnit())
                .period(entity.getPeriod())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
