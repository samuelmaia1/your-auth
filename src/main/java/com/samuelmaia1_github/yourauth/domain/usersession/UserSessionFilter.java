package com.samuelmaia1_github.yourauth.domain.usersession;

import java.time.Instant;

public record UserSessionFilter(
        UserSessionStatus status,
        Instant lastUsedAtFrom,
        Instant lastUsedAtTo,
        String userEmail
) {
    public UserSessionFilter {
        userEmail = normalize(userEmail);

        if (lastUsedAtFrom != null && lastUsedAtTo != null && lastUsedAtFrom.isAfter(lastUsedAtTo)) {
            throw new IllegalArgumentException("A data inicial do último uso não pode ser posterior à data final.");
        }
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
