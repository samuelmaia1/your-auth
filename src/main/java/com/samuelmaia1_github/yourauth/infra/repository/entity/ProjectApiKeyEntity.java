package com.samuelmaia1_github.yourauth.infra.repository.entity;

import com.samuelmaia1_github.yourauth.domain.projectapikey.ProjectApiKeyScope;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@Table(
        name = "project_api_keys",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_project_api_keys_key_id",
                        columnNames = "key_id"
                ),
                @UniqueConstraint(
                        name = "uk_project_api_keys_prefix",
                        columnNames = "prefix"
                )
        }
)
@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
public class ProjectApiKeyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private String id;

    @Column(name = "project_id", nullable = false)
    private String projectId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "key_id", nullable = false, updatable = false, length = 64)
    private String keyId;

    @Column(nullable = false, updatable = false, length = 128)
    private String prefix;

    @Column(name = "secret_hash", nullable = false, length = 64)
    private String secretHash;

    @Column(name = "secret_last_four", nullable = false, length = 4)
    private String secretLastFour;

    @Column(nullable = false, updatable = false, length = 30)
    private String environment;

    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "project_api_key_scopes",
            joinColumns = @JoinColumn(name = "project_api_key_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "scope", nullable = false, length = 50)
    private Set<ProjectApiKeyScope> scopes = new HashSet<>();

    @Column(name = "created_by_account_id", nullable = false, updatable = false)
    private String createdByAccountId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
}
