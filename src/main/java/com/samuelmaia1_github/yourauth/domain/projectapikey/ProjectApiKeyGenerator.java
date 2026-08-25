package com.samuelmaia1_github.yourauth.domain.projectapikey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Locale;

@Component
public class ProjectApiKeyGenerator {
    private static final String PREFIX = "ya_sk";
    private static final int KEY_ID_SIZE = 9;
    private static final int SECRET_SIZE = 32;

    private final SecureRandom secureRandom;
    private final String environment;

    public ProjectApiKeyGenerator(
            @Value("${api.security.project-api-key.environment}")
            String environment
    ) {
        this.secureRandom = new SecureRandom();
        this.environment = normalizeEnvironment(environment);
    }

    public GeneratedProjectApiKey generate() {
        String keyId = generateRandom(KEY_ID_SIZE);
        String secret = generateRandom(SECRET_SIZE);
        String prefix = "%s_%s_%s".formatted(PREFIX, environment, keyId);
        String rawKey = "%s.%s".formatted(prefix, secret);

        return new GeneratedProjectApiKey(
                keyId,
                prefix,
                secret.substring(secret.length() - 4),
                rawKey,
                environment
        );
    }

    private String generateRandom(int size) {
        byte[] randomBytes = new byte[size];
        secureRandom.nextBytes(randomBytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
    }

    private String normalizeEnvironment(String environment) {
        if (environment == null || environment.isBlank()) {
            throw new IllegalArgumentException("O ambiente da API key é obrigatório.");
        }

        String normalizedEnvironment = environment
                .trim()
                .toLowerCase(Locale.ROOT);

        if (!normalizedEnvironment.matches("[a-z0-9]+")) {
            throw new IllegalArgumentException(
                    "O ambiente da API key deve conter apenas letras minúsculas e números."
            );
        }

        return normalizedEnvironment;
    }
}
