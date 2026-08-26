package com.samuelmaia1_github.yourauth.domain.refreshtoken;

import java.util.List;
import java.util.Optional;

public interface UserRefreshTokenRepository {
    UserRefreshToken save(UserRefreshToken refreshToken);

    Optional<UserRefreshToken> findById(String id);

    Optional<UserRefreshToken> findByHash(String hash);

    List<UserRefreshToken> findAllByProjectIdAndUserId(String projectId, String userId);

    List<UserRefreshToken> findAllByFamilyId(String familyId);

    void revokeFamily(String familyId);

    void deleteById(String id);
}
