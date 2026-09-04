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
}
