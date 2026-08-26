package com.samuelmaia1_github.yourauth.infra.repository.adapter;

import com.samuelmaia1_github.yourauth.domain.refreshtoken.UserRefreshToken;
import com.samuelmaia1_github.yourauth.domain.refreshtoken.UserRefreshTokenRepository;
import com.samuelmaia1_github.yourauth.infra.mappers.UserRefreshTokenMapper;
import com.samuelmaia1_github.yourauth.infra.repository.UserRefreshTokenJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserRefreshTokenRepositoryAdapter implements UserRefreshTokenRepository {
    private final UserRefreshTokenJpaRepository repository;

    @Override
    public UserRefreshToken save(UserRefreshToken refreshToken) {
        return UserRefreshTokenMapper.toDomain(repository.save(UserRefreshTokenMapper.toEntity(refreshToken)));
    }

    @Override
    public Optional<UserRefreshToken> findById(String id) {
        return repository.findById(id).map(UserRefreshTokenMapper::toDomain);
    }

    @Override
    public Optional<UserRefreshToken> findByHash(String hash) {
        return repository.findByHash(hash).map(UserRefreshTokenMapper::toDomain);
    }

    @Override
    public List<UserRefreshToken> findAllByProjectIdAndUserId(String projectId, String userId) {
        return repository.findAllByProjectIdAndUserId(projectId, userId)
                .stream()
                .map(UserRefreshTokenMapper::toDomain)
                .toList();
    }

    @Override
    public List<UserRefreshToken> findAllBySessionId(String sessionId) {
        return repository.findAllBySessionId(sessionId)
                .stream()
                .map(UserRefreshTokenMapper::toDomain)
                .toList();
    }

    @Override
    public void revokeSession(String sessionId) {
        repository.revokeSession(sessionId);
    }

    @Override
    public void deleteById(String id) {
        repository.deleteById(id);
    }
}
