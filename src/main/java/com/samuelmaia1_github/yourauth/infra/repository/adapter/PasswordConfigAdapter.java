package com.samuelmaia1_github.yourauth.infra.repository.adapter;

import com.samuelmaia1_github.yourauth.domain.project.passwordconfig.PasswordConfig;
import com.samuelmaia1_github.yourauth.domain.project.passwordconfig.PasswordConfigRepository;
import com.samuelmaia1_github.yourauth.infra.mappers.PasswordConfigMapper;
import com.samuelmaia1_github.yourauth.infra.repository.PasswordConfigJpaRepository;
import com.samuelmaia1_github.yourauth.infra.repository.entity.PasswordConfigEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PasswordConfigAdapter implements PasswordConfigRepository {
    private final PasswordConfigJpaRepository repository;

    @Override
    public PasswordConfig save(PasswordConfig config) {
        PasswordConfigEntity createdEntity = repository.save(PasswordConfigMapper.toEntity(config));
        return PasswordConfigMapper.toDomain(createdEntity);
    }

    @Override
    public Optional<PasswordConfig> findByProjectId(String projectId) {
        return repository.findByProjectId(projectId).map(PasswordConfigMapper::toDomain);
    }
}
