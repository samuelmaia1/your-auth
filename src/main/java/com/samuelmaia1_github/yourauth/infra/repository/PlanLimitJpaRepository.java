package com.samuelmaia1_github.yourauth.infra.repository;

import com.samuelmaia1_github.yourauth.infra.repository.entity.PlanLimitEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PlanLimitJpaRepository extends JpaRepository<PlanLimitEntity, String> {
    List<PlanLimitEntity> findAllByPlanId(String planId);

    List<PlanLimitEntity> findAllByPlanIdIn(Collection<String> planIds);
}
