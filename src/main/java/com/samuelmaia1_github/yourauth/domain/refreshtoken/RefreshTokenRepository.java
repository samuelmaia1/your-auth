package com.samuelmaia1_github.yourauth.domain.refreshtoken;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository {
    RefreshToken save(RefreshToken refreshToken);

    Optional<RefreshToken> findById(String id);

    Optional<RefreshToken> findByHash(String hash);

    List<RefreshToken> findAllByUserId(String userId);

    List<RefreshToken> findAllByFamilyId(String familyId);

    void revokeFamily(String familyId);

    void deleteById(String id);
}
