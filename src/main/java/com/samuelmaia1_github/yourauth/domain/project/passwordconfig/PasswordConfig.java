package com.samuelmaia1_github.yourauth.domain.project.passwordconfig;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PasswordConfig {
    public static final int DEFAULT_MIN_SIZE = 1;
    public static final int DEFAULT_MAX_SIZE = 120;

    private String id;
    private String projectId;
    private boolean numberRequired;
    private boolean specialCharRequired;
    private boolean uppercaseRequired;
    private boolean lowercaseRequired;
    private int minSize;
    private int maxSize;

    public static PasswordConfig createDefault() {
        return PasswordConfig.builder()
                .minSize(DEFAULT_MIN_SIZE)
                .maxSize(DEFAULT_MAX_SIZE)
                .numberRequired(false)
                .specialCharRequired(false)
                .uppercaseRequired(false)
                .lowercaseRequired(false)
                .build();
    }

    public void assignToProject(String projectId) {
        if (projectId == null || projectId.isBlank()) {
            throw new IllegalArgumentException(
                    "O ID do projeto é obrigatório."
            );
        }

        if (this.projectId != null) {
            throw new IllegalStateException(
                    "A configuração já pertence a um projeto."
            );
        }

        this.projectId = projectId;
    }
}
