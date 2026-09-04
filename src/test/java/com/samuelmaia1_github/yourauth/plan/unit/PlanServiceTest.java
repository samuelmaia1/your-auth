package com.samuelmaia1_github.yourauth.plan.unit;

import com.samuelmaia1_github.yourauth.domain.plan.Plan;
import com.samuelmaia1_github.yourauth.domain.plan.PlanCode;
import com.samuelmaia1_github.yourauth.domain.plan.PlanRepository;
import com.samuelmaia1_github.yourauth.domain.plan.PlanService;
import org.junit.jupiter.api.Test;

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

        private RecordingPlanRepository(List<Plan> plans) {
            this.plans = plans;
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
    }
}
