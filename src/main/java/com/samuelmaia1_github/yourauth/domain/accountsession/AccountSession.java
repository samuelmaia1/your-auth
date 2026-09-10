package com.samuelmaia1_github.yourauth.domain.accountsession;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class AccountSession {
    private String id;
    private String accountId;
    private String deviceName;
    private String ipAddress;
    private String userAgent;
    private Instant createdAt;
    private Instant lastUsedAt;
    private Instant revokedAt;
    private long version;

    public boolean isValid() {
        return !isRevoked();
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public void revoke() {
        revokedAt = Instant.now();
    }

    public void refresh() {
        lastUsedAt = Instant.now();
    }
}
