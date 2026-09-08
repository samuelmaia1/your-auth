package com.samuelmaia1_github.yourauth.infra.repository.adapter;

import com.samuelmaia1_github.yourauth.domain.plan.Plan;
import com.samuelmaia1_github.yourauth.domain.plan.PlanCode;
import com.samuelmaia1_github.yourauth.domain.plan.PlanLimit;
import com.samuelmaia1_github.yourauth.domain.plan.PlanRepository;
import com.samuelmaia1_github.yourauth.infra.mappers.PlanMapper;
import com.samuelmaia1_github.yourauth.infra.repository.PlanFeatureJpaRepository;
import com.samuelmaia1_github.yourauth.infra.repository.PlanJpaRepository;
import com.samuelmaia1_github.yourauth.infra.repository.PlanLimitJpaRepository;
import com.samuelmaia1_github.yourauth.infra.repository.entity.PlanEntity;
import com.samuelmaia1_github.yourauth.infra.repository.entity.PlanFeatureEntity;
import com.samuelmaia1_github.yourauth.infra.repository.entity.PlanLimitEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PlanRepositoryAdapter implements PlanRepository {
    private final PlanJpaRepository planRepository;
    private final PlanFeatureJpaRepository featureRepository;
    private final PlanLimitJpaRepository limitRepository;

    @Override
    public List<Plan> findAllActive() {
        List<PlanEntity> plans = planRepository.findAllByActiveTrueOrderByDisplayOrderAsc();
        List<String> planIds = plans.stream()
                .map(PlanEntity::getId)
                .toList();

        if (planIds.isEmpty()) {
            return List.of();
        }

        Map<String, List<PlanFeatureEntity>> featuresByPlanId = featureRepository.findAllByPlanIdIn(planIds)
                .stream()
                .collect(Collectors.groupingBy(PlanFeatureEntity::getPlanId));
        Map<String, List<PlanLimitEntity>> limitsByPlanId = limitRepository.findAllByPlanIdIn(planIds)
                .stream()
                .collect(Collectors.groupingBy(PlanLimitEntity::getPlanId));

        return plans.stream()
                .map(plan -> PlanMapper.toDomain(
                        plan,
                        featuresByPlanId.getOrDefault(plan.getId(), List.of()),
                        limitsByPlanId.getOrDefault(plan.getId(), List.of())
                ))
                .toList();
    }

    @Override
    public Optional<Plan> findById(String id) {
        return planRepository.findById(id)
                .map(this::toPlanWithFeaturesAndLimits);
    }

    @Override
    public Optional<Plan> findByCode(PlanCode code) {
        return planRepository.findByCode(code)
                .map(this::toPlanWithFeaturesAndLimits);
    }

    @Override
    public List<PlanLimit> saveLimits(String planId, List<PlanLimit> limits) {
        List<String> codes = limits.stream()
                .map(PlanLimit::getCode)
                .toList();
        Map<String, PlanLimitEntity> existingLimitsByCode = limitRepository.findAllByPlanIdAndCodeIn(planId, codes)
                .stream()
                .collect(Collectors.toMap(PlanLimitEntity::getCode, limit -> limit));

        List<PlanLimitEntity> entities = limits.stream()
                .map(limit -> toLimitEntity(planId, limit, existingLimitsByCode.get(limit.getCode())))
                .toList();

        return limitRepository.saveAll(entities)
                .stream()
                .map(PlanMapper::toLimitDomain)
                .toList();
    }

    private Plan toPlanWithFeaturesAndLimits(PlanEntity plan) {
        return PlanMapper.toDomain(
                plan,
                featureRepository.findAllByPlanId(plan.getId()),
                limitRepository.findAllByPlanId(plan.getId())
        );
    }

    private static PlanLimitEntity toLimitEntity(String planId, PlanLimit limit, PlanLimitEntity currentLimit) {
        PlanLimitEntity entity = currentLimit == null
                ? PlanLimitEntity.builder()
                .planId(planId)
                .code(limit.getCode())
                .build()
                : currentLimit;

        entity.setValue(limit.getValue());
        entity.setUnit(limit.getUnit());
        entity.setPeriod(limit.getPeriod());

        return entity;
    }
}
