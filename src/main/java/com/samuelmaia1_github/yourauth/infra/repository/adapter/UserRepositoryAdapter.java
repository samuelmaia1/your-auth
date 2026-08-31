package com.samuelmaia1_github.yourauth.infra.repository.adapter;

import com.samuelmaia1_github.yourauth.domain.shared.PageResult;
import com.samuelmaia1_github.yourauth.domain.shared.Pagination;
import com.samuelmaia1_github.yourauth.domain.user.User;
import com.samuelmaia1_github.yourauth.domain.user.UserRepository;
import com.samuelmaia1_github.yourauth.infra.mappers.UserMapper;
import com.samuelmaia1_github.yourauth.infra.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {
    private final UserJpaRepository repository;

    @Override
    public User save(User user) {
        return UserMapper.toDomain(repository.save(UserMapper.toEntity(user)));
    }

    @Override
    public Optional<User> findById(String id) {
        return repository.findById(id).map(UserMapper::toDomain);
    }

    @Override
    public Optional<User> findByProjectIdAndId(String projectId, String id) {
        return repository.findByProjectIdAndId(projectId, id).map(UserMapper::toDomain);
    }

    @Override
    public Optional<User> findByProjectIdAndEmailIgnoreCase(String projectId, String email) {
        return repository.findByProjectIdAndEmailIgnoreCase(projectId, email).map(UserMapper::toDomain);
    }

    @Override
    public boolean existsByProjectIdAndEmailIgnoreCase(String projectId, String email) {
        return repository.existsByProjectIdAndEmailIgnoreCase(projectId, email);
    }

    @Override
    public boolean existsByProjectIdAndEmailIgnoreCaseAndIdNot(String projectId, String email, String id) {
        return repository.existsByProjectIdAndEmailIgnoreCaseAndIdNot(projectId, email, id);
    }

    @Override
    public PageResult<User> findAllByProjectId(String projectId, Pagination pagination) {
        Page<User> page = repository.findAllByProjectId(
                projectId,
                pageRequest(pagination)
        ).map(UserMapper::toDomain);

        return toPageResult(page);
    }

    @Override
    public void deleteById(String id) {
        repository.deleteById(id);
    }

    private PageRequest pageRequest(Pagination pagination) {
        return PageRequest.of(
                pagination.page(),
                pagination.size(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
    }

    private PageResult<User> toPageResult(Page<User> page) {
        return new PageResult<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
