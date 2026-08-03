package com.samuelmaia1_github.yourauth.infra.repository.adapter;

import com.samuelmaia1_github.yourauth.domain.refreshtoken.RefreshToken;
import com.samuelmaia1_github.yourauth.domain.refreshtoken.RefreshTokenRepository;
import com.samuelmaia1_github.yourauth.infra.mappers.RefreshTokenMapper;
import com.samuelmaia1_github.yourauth.infra.repository.RefreshTokenJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {
    private final RefreshTokenJpaRepository repository;

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        return RefreshTokenMapper.toDomain(repository.save(RefreshTokenMapper.toEntity(refreshToken)));
    }

    @Override
    public Optional<RefreshToken> findById(String id) {
        return repository.findById(id).map(RefreshTokenMapper::toDomain);
    }

    @Override
    public Optional<RefreshToken> findByHash(String hash) {
        return repository.findByHash(hash).map(RefreshTokenMapper::toDomain);
    }

    @Override
    public List<RefreshToken> findAllByUserId(String userId) {
        return repository.findAllByUserId(userId)
                .stream()
                .map(RefreshTokenMapper::toDomain)
                .toList();
    }

    @Override
    public List<RefreshToken> findAllByFamilyId(String familyId) {
        return repository.findAllByFamilyId(familyId)
                .stream()
                .map(RefreshTokenMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(String id) {
        repository.deleteById(id);
    }
}
