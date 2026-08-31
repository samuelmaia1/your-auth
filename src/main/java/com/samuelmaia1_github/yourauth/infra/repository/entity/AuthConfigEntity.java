package com.samuelmaia1_github.yourauth.infra.repository.entity;

import com.samuelmaia1_github.yourauth.domain.project.authconfig.SessionMode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "auth_config",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_auth_config_project",
                        columnNames = "project_id"
                )
        }
)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AuthConfigEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, length = 36)
    private String id;

    @Column(name = "project_id", nullable = false, length = 36)
    private String projectId;

    @Column(name = "access_token_expiration_minutes", nullable = false)
    private int accessTokenExpirationMinutes;

    @Column(name = "refresh_token_expiration_days", nullable = false)
    private int refreshTokenExpirationDays;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_mode", nullable = false, length = 40)
    private SessionMode sessionMode;

    @Column(name = "max_active_sessions")
    private Integer maxActiveSessions;

    @Column(name = "refresh_token_rotation_enabled", nullable = false)
    private boolean refreshTokenRotationEnabled;

    @Column(name = "revoke_tokens_on_password_change", nullable = false)
    private boolean revokeTokensOnPasswordChange;

    @Column(name = "failed_login_attempts_limit", nullable = false)
    private int failedLoginAttemptsLimit;

    @Column(name = "lock_duration_minutes", nullable = false)
    private int lockDurationMinutes;

    @Column(name = "require_email_verification", nullable = false)
    private boolean requireEmailVerification;

    @Column(name = "registration_enabled", nullable = false)
    private boolean registrationEnabled;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
