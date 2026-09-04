package com.samuelmaia1_github.yourauth.domain.projectapikey;

public record ProjectApiKeyFilter(String createdBy) {
    public ProjectApiKeyFilter {
        createdBy = normalize(createdBy);
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
