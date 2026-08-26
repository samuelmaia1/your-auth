package com.samuelmaia1_github.yourauth.domain.refreshtoken;

import java.util.List;
import java.util.Optional;

public interface AccountRefreshTokenRepository {
    AccountRefreshToken save(AccountRefreshToken refreshToken);

    Optional<AccountRefreshToken> findById(String id);

    Optional<AccountRefreshToken> findByHash(String hash);

    List<AccountRefreshToken> findAllByAccountId(String accountId);

    List<AccountRefreshToken> findAllByFamilyId(String familyId);

    void revokeFamily(String familyId);

    void deleteById(String id);
}
