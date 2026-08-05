package com.samuelmaia1_github.yourauth.domain.refreshtoken;

import com.samuelmaia1_github.yourauth.domain.refreshtoken.exceptions.RefreshTokenHashException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.HexFormat;

@Component
public class RefreshTokenHasher {

    private static final String ALGORITHM = "HmacSHA256";

    private final SecretKeySpec secretKey;

    public RefreshTokenHasher(
            @Value("${api.security.refresh-token.secret}")
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
            throw new RefreshTokenHashException(
                    "Falha ao calcular hash do refresh token",
                    exception
            );
        }
    }

    public boolean compareHashes(String hash1, String hash2) {
        return hash1.equals(hash2);
    }
}
