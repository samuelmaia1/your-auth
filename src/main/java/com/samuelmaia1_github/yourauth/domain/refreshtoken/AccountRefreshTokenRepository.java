package com.samuelmaia1_github.yourauth.domain.refreshtoken;

import java.util.List;
import java.util.Optional;

public interface AccountRefreshTokenRepository {
    AccountRefreshToken save(AccountRefreshToken refreshToken);

    Optional<AccountRefreshToken> findById(String id);

    Optional<AccountRefreshToken> findByHash(String hash);

    List<AccountRefreshToken> findAllByAccountId(String accountId);

    List<AccountRefreshToken> findAllBySessionId(String sessionId);

    void revokeSession(String sessionId);

    void deleteById(String id);
}
