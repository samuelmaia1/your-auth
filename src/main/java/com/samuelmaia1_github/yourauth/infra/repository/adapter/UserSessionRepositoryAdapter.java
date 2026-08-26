package com.samuelmaia1_github.yourauth.infra.repository.adapter;

import com.samuelmaia1_github.yourauth.domain.usersession.UserSession;
import com.samuelmaia1_github.yourauth.domain.usersession.UserSessionRepository;
import com.samuelmaia1_github.yourauth.infra.mappers.UserSessionMapper;
import com.samuelmaia1_github.yourauth.infra.repository.UserSessionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserSessionRepositoryAdapter implements UserSessionRepository {
    private final UserSessionJpaRepository repository;

    @Override
    public UserSession save(UserSession userSession) {
        return UserSessionMapper.toDomain(repository.save(UserSessionMapper.toEntity(userSession)));
    }

    @Override
    public Optional<UserSession> findById(String id) {
        return repository.findById(id).map(UserSessionMapper::toDomain);
    }

    @Override
    public List<UserSession> findAllByProjectIdAndUserId(String projectId, String userId) {
        return repository.findAllByProjectIdAndUserId(projectId, userId)
                .stream()
                .map(UserSessionMapper::toDomain)
                .toList();
    }

    @Override
    public List<UserSession> findAllByProjectIdAndUserIdAndRevokedAtIsNull(String projectId, String userId) {
        return repository.findAllByProjectIdAndUserIdAndRevokedAtIsNull(projectId, userId)
                .stream()
                .map(UserSessionMapper::toDomain)
                .toList();
    }

    @Override
    public void revokeById(String id) {
        repository.revokeById(id);
    }
}
