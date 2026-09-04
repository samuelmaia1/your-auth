package com.samuelmaia1_github.yourauth.domain.plan;

import java.util.List;
import java.util.Optional;

public interface PlanRepository {
    List<Plan> findAllActive();

    Optional<Plan> findById(String id);

    Optional<Plan> findByCode(PlanCode code);
}
