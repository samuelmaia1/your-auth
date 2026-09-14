package com.samuelmaia1_github.yourauth.domain.social;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SocialLoginCode {
    private String id;
    private String accountId;
    private String hash;
    private Instant expiresAt;
    private Instant consumedAt;
    private Instant createdAt;

    public boolean isExpired() {
        return expiresAt == null || !Instant.now().isBefore(expiresAt);
    }

    public boolean isConsumed() {
        return consumedAt != null;
    }
}
