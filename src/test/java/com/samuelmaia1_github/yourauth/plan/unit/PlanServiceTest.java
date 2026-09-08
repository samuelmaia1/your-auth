package com.samuelmaia1_github.yourauth.plan.unit;

import com.samuelmaia1_github.yourauth.domain.plan.Plan;
import com.samuelmaia1_github.yourauth.domain.plan.PlanCode;
import com.samuelmaia1_github.yourauth.domain.plan.PlanLimit;
import com.samuelmaia1_github.yourauth.domain.plan.PlanLimitCode;
import com.samuelmaia1_github.yourauth.domain.plan.PlanLimitSettings;
import com.samuelmaia1_github.yourauth.domain.plan.PlanRepository;
import com.samuelmaia1_github.yourauth.domain.plan.PlanService;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class PlanServiceTest {
    @Test
    void shouldReturnActivePlansFromRepository() {
        RecordingPlanRepository repository = new RecordingPlanRepository(List.of(
                plan("free", PlanCode.FREE),
                plan("starter", PlanCode.STARTER)
        ));
        PlanService service = new PlanService(repository);

        List<Plan> plans = service.findAllActive();

        assertThat(repository.findAllActiveCalled).isTrue();
        assertThat(plans).extracting(Plan::getCode)
                .containsExactly(PlanCode.FREE, PlanCode.STARTER);
    }

    @Test
    void shouldUpdatePlanLimits() {
        RecordingPlanRepository repository = new RecordingPlanRepository(List.of(
                plan("free", PlanCode.FREE)
        ));
        PlanService service = new PlanService(repository);

        Plan plan = service.updateLimits(PlanCode.FREE, new PlanLimitSettings(1L, 100L, 200L));

        assertThat(repository.savedPlanId).isEqualTo("free");
        assertThat(plan.getLimits())
                .extracting(PlanLimit::getCode, PlanLimit::getValue)
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple(PlanLimitCode.MAX_PROJECTS.name(), 1L),
                        org.assertj.core.groups.Tuple.tuple(PlanLimitCode.MAX_USERS_TOTAL.name(), 100L),
                        org.assertj.core.groups.Tuple.tuple(PlanLimitCode.MAX_ACTIVE_SESSIONS_TOTAL.name(), 200L)
                );
    }

    private static Plan plan(String id, PlanCode code) {
        return Plan.builder()
                .id(id)
                .code(code)
                .name(code.name())
                .active(true)
                .build();
    }

    private static class RecordingPlanRepository implements PlanRepository {
        private final List<Plan> plans;
        private boolean findAllActiveCalled;
        private String savedPlanId;

        private RecordingPlanRepository(List<Plan> plans) {
            this.plans = new ArrayList<>(plans);
        }

        @Override
        public List<Plan> findAllActive() {
            findAllActiveCalled = true;
            return plans;
        }

        @Override
        public Optional<Plan> findById(String id) {
            return plans.stream()
                    .filter(plan -> plan.getId().equals(id))
                    .findFirst();
        }

        @Override
        public Optional<Plan> findByCode(PlanCode code) {
            return plans.stream()
                    .filter(plan -> plan.getCode() == code)
                    .findFirst();
        }

        @Override
        public List<PlanLimit> saveLimits(String planId, List<PlanLimit> limits) {
            savedPlanId = planId;

            for (int i = 0; i < plans.size(); i++) {
                Plan plan = plans.get(i);

                if (plan.getId().equals(planId)) {
                    plans.set(i, plan.toBuilder()
                            .limits(limits)
                            .build());
                }
            }

            return limits;
        }
    }
}
