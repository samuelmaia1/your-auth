package com.samuelmaia1_github.yourauth.domain.project.passwordconfig;

public record PasswordConfigUpdate(
        Integer minSize,
        boolean minSizeProvided,
        Integer maxSize,
        boolean maxSizeProvided,
        Boolean numberRequired,
        boolean numberRequiredProvided,
        Boolean uppercaseRequired,
        boolean uppercaseRequiredProvided,
        Boolean lowercaseRequired,
        boolean lowercaseRequiredProvided,
        Boolean specialCharRequired,
        boolean specialCharRequiredProvided
) {
}
