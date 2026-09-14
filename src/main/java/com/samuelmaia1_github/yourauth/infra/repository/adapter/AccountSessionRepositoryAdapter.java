package com.samuelmaia1_github.yourauth.infra.repository.adapter;

import com.samuelmaia1_github.yourauth.domain.accountsession.AccountSession;
import com.samuelmaia1_github.yourauth.domain.accountsession.AccountSessionRepository;
import com.samuelmaia1_github.yourauth.infra.mappers.AccountSessionMapper;
import com.samuelmaia1_github.yourauth.infra.repository.AccountSessionJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountSessionRepositoryAdapter implements AccountSessionRepository {
    private final AccountSessionJpaRepository repository;

    @Override
    public AccountSession save(AccountSession accountSession) {
        log.debug(
                "Persistindo sessao de conta: sessionId={}, accountId={}, revoked={}, lastUsedAt={}",
                accountSession.getId(),
                accountSession.getAccountId(),
                accountSession.isRevoked(),
                accountSession.getLastUsedAt()
        );
        AccountSession savedSession = AccountSessionMapper.toDomain(repository.save(AccountSessionMapper.toEntity(accountSession)));
        log.debug(
                "Sessao de conta persistida no banco: sessionId={}, accountId={}, revoked={}, createdAt={}, lastUsedAt={}",
                savedSession.getId(),
                savedSession.getAccountId(),
                savedSession.isRevoked(),
                savedSession.getCreatedAt(),
                savedSession.getLastUsedAt()
        );
        return savedSession;
    }

    @Override
    public Optional<AccountSession> findById(String id) {
        Optional<AccountSession> session = repository.findById(id).map(AccountSessionMapper::toDomain);
        log.debug("Busca de sessao de conta por id: sessionId={}, found={}", id, session.isPresent());
        return session;
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
        int updatedRows = repository.revokeById(id);
        log.debug("Revogacao de sessao de conta por id executada: sessionId={}, updatedRows={}", id, updatedRows);
    }

    @Override
    public void revokeAllByAccountId(String accountId) {
        int updatedRows = repository.revokeAllByAccountId(accountId);
        log.debug("Revogacao de sessoes de conta por accountId executada: accountId={}, updatedRows={}", accountId, updatedRows);
    }

    @Override
    public long countByAccountIdAndRevokedAtIsNull(String accountId) {
        long count = repository.countByAccountIdAndRevokedAtIsNull(accountId);
        log.debug("Contagem de sessoes ativas de conta executada: accountId={}, activeSessions={}", accountId, count);
        return count;
    }
}
