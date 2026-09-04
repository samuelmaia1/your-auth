package com.samuelmaia1_github.yourauth.domain.user;

public record UserFilter(
        String email,
        UserStatus status
) {
    public UserFilter {
        email = normalize(email);
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
