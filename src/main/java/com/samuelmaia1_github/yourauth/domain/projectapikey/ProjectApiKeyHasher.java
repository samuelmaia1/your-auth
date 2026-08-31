package com.samuelmaia1_github.yourauth.domain.projectapikey;

import com.samuelmaia1_github.yourauth.domain.projectapikey.exceptions.ProjectApiKeyHashException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.HexFormat;

@Component
public class ProjectApiKeyHasher {
    private static final String ALGORITHM = "HmacSHA256";

    private final SecretKeySpec secretKey;

    public ProjectApiKeyHasher(
            @Value("${api.security.project-api-key.secret}")
            String secret
    ) {
        this.secretKey = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                ALGORITHM
        );
    }

    public String hash(String raw) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(secretKey);

            byte[] result = mac.doFinal(
                    raw.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(result);
        } catch (GeneralSecurityException exception) {
            throw new ProjectApiKeyHashException(
                    "Falha ao calcular hash da API key do projeto",
                    exception
            );
        }
    }

    public boolean compareHashes(String hash1, String hash2) {
        if (hash1 == null || hash2 == null) {
            return false;
        }

        return MessageDigest.isEqual(
                hash1.getBytes(StandardCharsets.UTF_8),
                hash2.getBytes(StandardCharsets.UTF_8)
        );
    }
}
