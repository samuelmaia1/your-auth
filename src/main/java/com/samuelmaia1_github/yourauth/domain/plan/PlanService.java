package com.samuelmaia1_github.yourauth.domain.plan;

import com.samuelmaia1_github.yourauth.domain.plan.exceptions.PlanNotFoundException;
import com.samuelmaia1_github.yourauth.infra.cache.names.PlanCacheNames;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.samuelmaia1_github.yourauth.infra.cache.names.SubscriptionCacheNames.*;

@Service
@RequiredArgsConstructor
public class PlanService {
    private final PlanRepository repository;

    @Cacheable(
            cacheNames = PlanCacheNames.PLANS_CACHE,
            key = "'allPlans'"
    )
    public List<Plan> findAllActive() {
        return repository.findAllActive();
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(
                    cacheNames = PlanCacheNames.PLANS_CACHE,
                    key = "'allPlans'"
            ),
            @CacheEvict(
                    cacheNames = CURRENT_BY_ACCOUNT_ID,
                    allEntries = true
            )
    })
    public Plan updateLimits(PlanCode code, PlanLimitSettings settings) {
        Plan plan = repository.findByCode(code)
                .orElseThrow(PlanNotFoundException::new);

        if (settings.hasChanges()) {
            repository.saveLimits(plan.getId(), settings.toLimits(plan.getId()));
        }

        return repository.findById(plan.getId())
                .orElseThrow(PlanNotFoundException::new);
    }
}
