package com.samuelmaia1_github.yourauth.domain.user;

import com.samuelmaia1_github.yourauth.domain.shared.Phone;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private String id;
    private String projectId;
    private String email;
    private String password;
    private UserStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
    private LocalDateTime lastPasswordChangedAt;
    private LocalDateTime lastFailedLoginAt;
    private Integer failedLoginAttempts;
    private LocalDateTime lockedUntil;
    private String lastLoginIpAddress;
    private String lastLoginUserAgent;
    private Phone phone;

    public void updatePassword(String password) {
        this.password = password;
        this.lastPasswordChangedAt = LocalDateTime.now();
    }

    public void activate() {
        this.status = UserStatus.ACTIVE;
    }

    public void disable() {
        this.status = UserStatus.DISABLED;
    }

    public boolean canAuthenticate() {
        return UserStatus.ACTIVE.equals(status) && !isLocked();
    }

    public boolean isLocked() {
        return lockedUntil != null && LocalDateTime.now().isBefore(lockedUntil);
    }

    public void recordSuccessfulLogin(String ipAddress, String userAgent) {
        this.lastLoginAt = LocalDateTime.now();
        this.lastLoginIpAddress = ipAddress;
        this.lastLoginUserAgent = userAgent;
        this.failedLoginAttempts = 0;
        this.lastFailedLoginAt = null;
        this.lockedUntil = null;
    }

    public void recordFailedLogin() {
        this.lastFailedLoginAt = LocalDateTime.now();
        this.failedLoginAttempts = failedLoginAttempts == null ? 1 : failedLoginAttempts + 1;
    }

    public void lockUntil(LocalDateTime lockedUntil) {
        this.lockedUntil = lockedUntil;
    }
}
