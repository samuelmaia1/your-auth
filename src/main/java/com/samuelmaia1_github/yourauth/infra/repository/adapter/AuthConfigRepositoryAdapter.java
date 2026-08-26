package com.samuelmaia1_github.yourauth.infra.repository.adapter;

import com.samuelmaia1_github.yourauth.domain.project.authconfig.AuthConfig;
import com.samuelmaia1_github.yourauth.domain.project.authconfig.AuthConfigRepository;
import com.samuelmaia1_github.yourauth.infra.mappers.AuthConfigMapper;
import com.samuelmaia1_github.yourauth.infra.repository.AuthConfigJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AuthConfigRepositoryAdapter implements AuthConfigRepository {
    private final AuthConfigJpaRepository repository;

    @Override
    public AuthConfig save(AuthConfig config) {
        return AuthConfigMapper.toDomain(repository.save(AuthConfigMapper.toEntity(config)));
    }

    @Override
    public Optional<AuthConfig> findByProjectId(String projectId) {
        return repository.findByProjectId(projectId).map(AuthConfigMapper::toDomain);
    }
}
