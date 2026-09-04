package com.samuelmaia1_github.yourauth.domain.plan;

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
}
