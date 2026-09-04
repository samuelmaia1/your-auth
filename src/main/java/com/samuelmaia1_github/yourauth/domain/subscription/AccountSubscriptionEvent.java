package com.samuelmaia1_github.yourauth.domain.subscription;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountSubscriptionEvent {
    private String id;
    private String accountId;
    private String subscriptionId;
    private AccountSubscriptionEventType eventType;
    private String previousPlanId;
    private String newPlanId;
    private LocalDateTime occurredAt;
    private String description;
    private String createdByAccountId;
    private LocalDateTime createdAt;
}
