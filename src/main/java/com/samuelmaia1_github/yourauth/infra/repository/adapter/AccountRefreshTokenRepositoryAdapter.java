package com.samuelmaia1_github.yourauth.infra.repository.adapter;

import com.samuelmaia1_github.yourauth.domain.refreshtoken.AccountRefreshToken;
import com.samuelmaia1_github.yourauth.domain.refreshtoken.AccountRefreshTokenRepository;
import com.samuelmaia1_github.yourauth.infra.mappers.AccountRefreshTokenMapper;
import com.samuelmaia1_github.yourauth.infra.repository.AccountRefreshTokenJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AccountRefreshTokenRepositoryAdapter implements AccountRefreshTokenRepository {
    private final AccountRefreshTokenJpaRepository repository;

    @Override
    public AccountRefreshToken save(AccountRefreshToken refreshToken) {
        return AccountRefreshTokenMapper.toDomain(repository.save(AccountRefreshTokenMapper.toEntity(refreshToken)));
    }

    @Override
    public Optional<AccountRefreshToken> findById(String id) {
        return repository.findById(id).map(AccountRefreshTokenMapper::toDomain);
    }

    @Override
    public Optional<AccountRefreshToken> findByHash(String hash) {
        return repository.findByHash(hash).map(AccountRefreshTokenMapper::toDomain);
    }

    @Override
    public List<AccountRefreshToken> findAllByAccountId(String accountId) {
        return repository.findAllByAccountId(accountId)
                .stream()
                .map(AccountRefreshTokenMapper::toDomain)
                .toList();
    }

    @Override
    public List<AccountRefreshToken> findAllByFamilyId(String familyId) {
        return repository.findAllByFamilyId(familyId)
                .stream()
                .map(AccountRefreshTokenMapper::toDomain)
                .toList();
    }

    @Override
    public void revokeFamily(String familyId) {
        repository.revokeFamily(familyId);
    }

    @Override
    public void deleteById(String id) {
        repository.deleteById(id);
    }
}
