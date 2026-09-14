package com.samuelmaia1_github.yourauth.domain.project;

public record ProjectUpdate(
        String name,
        boolean nameProvided,
        String description,
        boolean descriptionProvided,
        ProjectStatus status,
        boolean statusProvided,
        ProjectEnvironment environment,
        boolean environmentProvided,
        String tokenAudience,
        boolean tokenAudienceProvided
) {
}
