package com.samuelmaia1_github.yourauth.domain.plan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class Plan {
    private String id;
    private PlanCode code;
    private String name;
    private String description;
    private boolean active;
    private int displayOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<PlanFeature> features;
    private List<PlanLimit> limits;

    public String getId() {
        return id;
    }

    public PlanCode getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return active;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<PlanFeature> getFeatures() {
        return features == null ? List.of() : List.copyOf(features);
    }

    public List<PlanLimit> getLimits() {
        return limits == null ? List.of() : List.copyOf(limits);
    }
}
