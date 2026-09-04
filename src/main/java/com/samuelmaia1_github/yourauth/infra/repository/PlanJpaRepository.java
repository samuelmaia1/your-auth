package com.samuelmaia1_github.yourauth.infra.repository;

import com.samuelmaia1_github.yourauth.domain.plan.PlanCode;
import com.samuelmaia1_github.yourauth.infra.repository.entity.PlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlanJpaRepository extends JpaRepository<PlanEntity, String> {
    List<PlanEntity> findAllByActiveTrueOrderByDisplayOrderAsc();

    Optional<PlanEntity> findByCode(PlanCode code);
}
