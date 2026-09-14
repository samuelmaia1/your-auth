package com.samuelmaia1_github.yourauth.domain.social;

import java.time.Instant;
import java.util.Optional;

public interface SocialLoginCodeRepository {
    SocialLoginCode save(SocialLoginCode code);

    Optional<SocialLoginCode> findByHash(String hash);

    Optional<SocialLoginCode> consumeValid(String hash, Instant consumedAt);
}
