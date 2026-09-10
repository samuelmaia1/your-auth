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
import static org.assertj.core.groups.Tuple.tuple;

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
                        tuple(PlanLimitCode.MAX_PROJECTS.name(), 1L),
                        tuple(PlanLimitCode.MAX_USERS_TOTAL.name(), 100L),
                        tuple(PlanLimitCode.MAX_ACTIVE_SESSIONS_TOTAL.name(), 200L)
                );
    }

    @Test
    void shouldUpdateOnlyProvidedPlanLimits() {
        RecordingPlanRepository repository = new RecordingPlanRepository(List.of(
                plan("free", PlanCode.FREE, List.of(
                        limit("free", PlanLimitCode.MAX_PROJECTS, 1L),
                        limit("free", PlanLimitCode.MAX_USERS_TOTAL, 100L),
                        limit("free", PlanLimitCode.MAX_ACTIVE_SESSIONS_TOTAL, 200L)
                ))
        ));
        PlanService service = new PlanService(repository);
        PlanLimitSettings settings = new PlanLimitSettings(
                2L,
                true,
                null,
                false,
                null,
                true
        );

        Plan plan = service.updateLimits(PlanCode.FREE, settings);

        assertThat(repository.savedPlanId).isEqualTo("free");
        assertThat(repository.savedLimits)
                .extracting(PlanLimit::getCode, PlanLimit::getValue)
                .containsExactlyInAnyOrder(
                        tuple(PlanLimitCode.MAX_PROJECTS.name(), 2L),
                        tuple(PlanLimitCode.MAX_ACTIVE_SESSIONS_TOTAL.name(), null)
                );
        assertThat(plan.getLimits())
                .extracting(PlanLimit::getCode, PlanLimit::getValue)
                .containsExactlyInAnyOrder(
                        tuple(PlanLimitCode.MAX_PROJECTS.name(), 2L),
                        tuple(PlanLimitCode.MAX_USERS_TOTAL.name(), 100L),
                        tuple(PlanLimitCode.MAX_ACTIVE_SESSIONS_TOTAL.name(), null)
                );
    }

    @Test
    void shouldReturnPlanWithoutSavingWhenNoLimitWasProvided() {
        RecordingPlanRepository repository = new RecordingPlanRepository(List.of(
                plan("free", PlanCode.FREE, List.of(
                        limit("free", PlanLimitCode.MAX_PROJECTS, 1L),
                        limit("free", PlanLimitCode.MAX_USERS_TOTAL, 100L)
                ))
        ));
        PlanService service = new PlanService(repository);

        Plan plan = service.updateLimits(PlanCode.FREE, new PlanLimitSettings(
                null,
                false,
                null,
                false,
                null,
                false
        ));

        assertThat(repository.savedPlanId).isNull();
        assertThat(repository.savedLimits).isEmpty();
        assertThat(plan.getLimits())
                .extracting(PlanLimit::getCode, PlanLimit::getValue)
                .containsExactlyInAnyOrder(
                        tuple(PlanLimitCode.MAX_PROJECTS.name(), 1L),
                        tuple(PlanLimitCode.MAX_USERS_TOTAL.name(), 100L)
                );
    }

    private static Plan plan(String id, PlanCode code, List<PlanLimit> limits) {
        return Plan.builder()
                .id(id)
                .code(code)
                .name(code.name())
                .active(true)
                .limits(limits)
                .build();
    }

    private static Plan plan(String id, PlanCode code) {
        return plan(id, code, List.of());
    }

    private static PlanLimit limit(String planId, PlanLimitCode code, Long value) {
        return PlanLimit.countLimit(planId, code, value);
    }

    private static class RecordingPlanRepository implements PlanRepository {
        private final List<Plan> plans;
        private boolean findAllActiveCalled;
        private String savedPlanId;
        private List<PlanLimit> savedLimits = List.of();

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
            savedLimits = limits;

            for (int i = 0; i < plans.size(); i++) {
                Plan plan = plans.get(i);

                if (plan.getId().equals(planId)) {
                    plans.set(i, plan.toBuilder()
                            .limits(mergeLimits(plan.getLimits(), limits))
                            .build());
                }
            }

            return limits;
        }

        private static List<PlanLimit> mergeLimits(List<PlanLimit> currentLimits, List<PlanLimit> newLimits) {
            List<PlanLimit> mergedLimits = new ArrayList<>(currentLimits);

            for (PlanLimit limit : newLimits) {
                mergedLimits.removeIf(currentLimit -> currentLimit.getCode().equals(limit.getCode()));
                mergedLimits.add(limit);
            }

            return mergedLimits;
        }
    }
}
