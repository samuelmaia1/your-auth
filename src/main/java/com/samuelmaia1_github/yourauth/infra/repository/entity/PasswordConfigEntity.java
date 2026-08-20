package com.samuelmaia1_github.yourauth.infra.repository.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "password_config",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_password_config_project",
                        columnNames = "project_id"
                )
        }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PasswordConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, length = 36)
    private String id;

    @Column(name = "project_id", nullable = false, length = 36)
    private String projectId;

    @Column(name = "number_required", nullable = false)
    private boolean numberRequired;

    @Column(name = "special_char_required", nullable = false)
    private boolean specialCharRequired;

    @Column(name = "uppercase_required", nullable = false)
    private boolean uppercaseRequired;

    @Column(name = "lowercase_required", nullable = false)
    private boolean lowercaseRequired;

    @Column(name = "min_size", nullable = false)
    private int minSize;

    @Column(name = "max_size", nullable = false)
    private int maxSize;
}
