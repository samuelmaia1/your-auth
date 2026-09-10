package com.samuelmaia1_github.yourauth.domain.accountsession;

import java.util.List;
import java.util.Optional;

public interface AccountSessionRepository {
    AccountSession save(AccountSession accountSession);

    Optional<AccountSession> findById(String id);

    List<AccountSession> findAllByAccountId(String accountId);

    List<AccountSession> findAllByAccountIdAndRevokedAtIsNull(String accountId);

    void revokeById(String id);

    void revokeAllByAccountId(String accountId);

    long countByAccountIdAndRevokedAtIsNull(String accountId);
}
