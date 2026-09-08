package com.samuelmaia1_github.yourauth.domain.plan;

import com.samuelmaia1_github.yourauth.domain.plan.exceptions.PlanNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanService {
    private final PlanRepository repository;

    public List<Plan> findAllActive() {
        return repository.findAllActive();
    }

    @Transactional
    public Plan updateLimits(PlanCode code, PlanLimitSettings settings) {
        Plan plan = repository.findByCode(code)
                .orElseThrow(PlanNotFoundException::new);

        repository.saveLimits(plan.getId(), settings.toLimits(plan.getId()));

        return repository.findById(plan.getId())
                .orElseThrow(PlanNotFoundException::new);
    }
}
