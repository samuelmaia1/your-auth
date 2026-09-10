package com.samuelmaia1_github.yourauth.infra.repository.adapter;

import com.samuelmaia1_github.yourauth.domain.accountsession.AccountSession;
import com.samuelmaia1_github.yourauth.domain.accountsession.AccountSessionRepository;
import com.samuelmaia1_github.yourauth.infra.mappers.AccountSessionMapper;
import com.samuelmaia1_github.yourauth.infra.repository.AccountSessionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AccountSessionRepositoryAdapter implements AccountSessionRepository {
    private final AccountSessionJpaRepository repository;

    @Override
    public AccountSession save(AccountSession accountSession) {
        return AccountSessionMapper.toDomain(repository.save(AccountSessionMapper.toEntity(accountSession)));
    }

    @Override
    public Optional<AccountSession> findById(String id) {
        return repository.findById(id).map(AccountSessionMapper::toDomain);
    }

    @Override
    public List<AccountSession> findAllByAccountId(String accountId) {
        return repository.findAllByAccountId(accountId)
                .stream()
                .map(AccountSessionMapper::toDomain)
                .toList();
    }

    @Override
    public List<AccountSession> findAllByAccountIdAndRevokedAtIsNull(String accountId) {
        return repository.findAllByAccountIdAndRevokedAtIsNull(accountId)
                .stream()
                .map(AccountSessionMapper::toDomain)
                .toList();
    }

    @Override
    public void revokeById(String id) {
        repository.revokeById(id);
    }

    @Override
    public void revokeAllByAccountId(String accountId) {
        repository.revokeAllByAccountId(accountId);
    }

    @Override
    public long countByAccountIdAndRevokedAtIsNull(String accountId) {
        return repository.countByAccountIdAndRevokedAtIsNull(accountId);
    }
}
