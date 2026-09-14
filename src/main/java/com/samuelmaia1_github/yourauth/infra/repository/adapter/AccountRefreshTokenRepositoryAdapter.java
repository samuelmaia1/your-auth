package com.samuelmaia1_github.yourauth.infra.repository.adapter;

import com.samuelmaia1_github.yourauth.domain.refreshtoken.AccountRefreshToken;
import com.samuelmaia1_github.yourauth.domain.refreshtoken.AccountRefreshTokenRepository;
import com.samuelmaia1_github.yourauth.infra.mappers.AccountRefreshTokenMapper;
import com.samuelmaia1_github.yourauth.infra.repository.AccountRefreshTokenJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.samuelmaia1_github.yourauth.domain.shared.SafeLog.fingerprint;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountRefreshTokenRepositoryAdapter implements AccountRefreshTokenRepository {
    private final AccountRefreshTokenJpaRepository repository;

    @Override
    public AccountRefreshToken save(AccountRefreshToken refreshToken) {
        log.debug(
                "Persistindo refresh token de conta: tokenId={}, accountId={}, sessionId={}, tokenFingerprint={}, revoked={}, expiresAt={}",
                refreshToken.getId(),
                refreshToken.getAccountId(),
                refreshToken.getSessionId(),
                fingerprint(refreshToken.getHash()),
                refreshToken.isRevoked(),
                refreshToken.getExpiresAt()
        );
        AccountRefreshToken savedToken = AccountRefreshTokenMapper.toDomain(repository.save(AccountRefreshTokenMapper.toEntity(refreshToken)));
        log.debug(
                "Refresh token de conta persistido no banco: tokenId={}, accountId={}, sessionId={}, tokenFingerprint={}, revoked={}, expiresAt={}",
                savedToken.getId(),
                savedToken.getAccountId(),
                savedToken.getSessionId(),
                fingerprint(savedToken.getHash()),
                savedToken.isRevoked(),
                savedToken.getExpiresAt()
        );
        return savedToken;
    }

    @Override
    public Optional<AccountRefreshToken> findById(String id) {
        return repository.findById(id).map(AccountRefreshTokenMapper::toDomain);
    }

    @Override
    public Optional<AccountRefreshToken> findByHash(String hash) {
        Optional<AccountRefreshToken> token = repository.findByHash(hash).map(AccountRefreshTokenMapper::toDomain);
        log.debug(
                "Busca de refresh token de conta por hash: tokenFingerprint={}, found={}",
                fingerprint(hash),
                token.isPresent()
        );
        return token;
    }

    @Override
    public List<AccountRefreshToken> findAllByAccountId(String accountId) {
        return repository.findAllByAccountId(accountId)
                .stream()
                .map(AccountRefreshTokenMapper::toDomain)
                .toList();
    }

    @Override
    public List<AccountRefreshToken> findAllBySessionId(String sessionId) {
        return repository.findAllBySessionId(sessionId)
                .stream()
                .map(AccountRefreshTokenMapper::toDomain)
                .toList();
    }

    @Override
    public void revokeSession(String sessionId) {
        int updatedRows = repository.revokeSession(sessionId);
        log.debug("Revogacao de refresh tokens por sessao executada: sessionId={}, updatedRows={}", sessionId, updatedRows);
    }

    @Override
    public void deleteById(String id) {
        repository.deleteById(id);
    }
}
