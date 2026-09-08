package com.samuelmaia1_github.yourauth.domain.plan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlanLimit {
    private static final String COUNT_UNIT = "COUNT";

    private String id;
    private String planId;
    private String code;
    private Long value;
    private String unit;
    private PlanLimitPeriod period;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public boolean isUnlimited() {
        return value == null;
    }

    public boolean hasCode(PlanLimitCode code) {
        return code != null && code.name().equals(this.code);
    }

    public static PlanLimit countLimit(String planId, PlanLimitCode code, Long value) {
        return PlanLimit.builder()
                .planId(planId)
                .code(code.name())
                .value(value)
                .unit(COUNT_UNIT)
                .period(PlanLimitPeriod.NONE)
                .build();
    }
}
