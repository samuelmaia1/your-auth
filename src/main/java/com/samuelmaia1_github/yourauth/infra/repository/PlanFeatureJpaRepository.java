package com.samuelmaia1_github.yourauth.infra.repository;

import com.samuelmaia1_github.yourauth.infra.repository.entity.PlanFeatureEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PlanFeatureJpaRepository extends JpaRepository<PlanFeatureEntity, String> {
    List<PlanFeatureEntity> findAllByPlanId(String planId);

    List<PlanFeatureEntity> findAllByPlanIdIn(Collection<String> planIds);
}
