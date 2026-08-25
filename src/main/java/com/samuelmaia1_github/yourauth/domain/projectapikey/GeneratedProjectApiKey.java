package com.samuelmaia1_github.yourauth.domain.projectapikey;

public record GeneratedProjectApiKey(
        String keyId,
        String prefix,
        String secretLastFour,
        String rawKey,
        String environment
) {
}
