package com.samuelmaia1_github.yourauth.domain.usersession;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class UserSession {
    private String id;
    private String projectId;
    private String userId;
    private String deviceName;
    private String ipAddress;
    private String userAgent;
    private Instant createdAt;
    private Instant lastUsedAt;
    private Instant revokedAt;
    private long version;
}
