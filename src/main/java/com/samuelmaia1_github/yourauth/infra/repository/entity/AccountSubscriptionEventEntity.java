package com.samuelmaia1_github.yourauth.infra.repository.entity;

import com.samuelmaia1_github.yourauth.domain.subscription.AccountSubscriptionEventType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@Table(name = "account_subscription_events")
@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
public class AccountSubscriptionEventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, length = 36)
    private String id;

    @Column(name = "account_id", nullable = false, length = 36)
    private String accountId;

    @Column(name = "subscription_id", nullable = false, length = 36)
    private String subscriptionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    private AccountSubscriptionEventType eventType;

    @Column(name = "previous_plan_id", length = 36)
    private String previousPlanId;

    @Column(name = "new_plan_id", length = 36)
    private String newPlanId;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @Column(length = 255)
    private String description;

    @Column(name = "created_by_account_id", length = 36)
    private String createdByAccountId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
