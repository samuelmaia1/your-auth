package com.samuelmaia1_github.yourauth.domain.projectapikey;

import com.samuelmaia1_github.yourauth.domain.auth.AuthenticatedProjectApiKey;
import com.samuelmaia1_github.yourauth.domain.projectapikey.exceptions.ExpiredProjectApiKeyException;
import com.samuelmaia1_github.yourauth.domain.projectapikey.exceptions.InvalidProjectApiKeyCredentialsException;
import com.samuelmaia1_github.yourauth.domain.projectapikey.exceptions.RevokedProjectApiKeyException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class ProjectApiKeyAuthenticationService {
    private static final String KEY_PREFIX = "ya_sk_";

    private final ProjectApiKeyRepository repository;
    private final ProjectApiKeyHasher hasher;

    @Transactional
    public AuthenticatedProjectApiKey authenticate(String rawApiKey) {
        String prefix = extractPrefix(rawApiKey);
        ProjectApiKey apiKey = repository
                .findByPrefix(prefix)
                .orElseThrow(InvalidProjectApiKeyCredentialsException::new);

        if (!hasher.compareHashes(hasher.hash(rawApiKey), apiKey.getSecretHash())) {
            throw new InvalidProjectApiKeyCredentialsException();
        }

        if (apiKey.isRevoked()) {
            throw new RevokedProjectApiKeyException();
        }

        if (apiKey.isExpired()) {
            throw new ExpiredProjectApiKeyException();
        }

        apiKey.markAsUsed();
        ProjectApiKey savedApiKey = repository.save(apiKey);

        return new AuthenticatedProjectApiKey(
                savedApiKey.getId(),
                savedApiKey.getProjectId(),
                savedApiKey.getKeyId(),
                savedApiKey.getScopes() == null ? new HashSet<>() : new HashSet<>(savedApiKey.getScopes())
        );
    }

    private String extractPrefix(String rawApiKey) {
        if (rawApiKey == null || rawApiKey.isBlank()) {
            throw new InvalidProjectApiKeyCredentialsException("API key não informada.");
        }

        String normalizedApiKey = rawApiKey.trim();
        int separatorIndex = normalizedApiKey.indexOf('.');

        if (
                separatorIndex <= 0
                        || separatorIndex == normalizedApiKey.length() - 1
                        || !normalizedApiKey.startsWith(KEY_PREFIX)
        ) {
            throw new InvalidProjectApiKeyCredentialsException();
        }

        return normalizedApiKey.substring(0, separatorIndex);
    }
}
