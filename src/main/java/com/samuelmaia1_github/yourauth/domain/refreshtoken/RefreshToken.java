package com.samuelmaia1_github.yourauth.domain.refreshtoken;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefreshToken {
    private String id;
    private String userId;
    private String hash;
    private String familyId;
    private Instant expiresAt;
    private Instant revokedAt;
    private Instant createdAt;
    private String userAgent;
    private long version;

    public void revoke() {
        this.revokedAt = Instant.now();
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !isRevoked() && !isExpired();
    }
}
