package com.samuelmaia1_github.yourauth.infra.repository.adapter;

import com.samuelmaia1_github.yourauth.domain.shared.PageResult;
import com.samuelmaia1_github.yourauth.domain.shared.Pagination;
import com.samuelmaia1_github.yourauth.domain.usersession.UserSession;
import com.samuelmaia1_github.yourauth.domain.usersession.UserSessionDetails;
import com.samuelmaia1_github.yourauth.domain.usersession.UserSessionDetailsRepository;
import com.samuelmaia1_github.yourauth.infra.mappers.UserMapper;
import com.samuelmaia1_github.yourauth.domain.usersession.UserSessionRepository;
import com.samuelmaia1_github.yourauth.infra.repository.UserSessionDetailsProjection;
import com.samuelmaia1_github.yourauth.infra.mappers.UserSessionMapper;
import com.samuelmaia1_github.yourauth.infra.repository.UserSessionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserSessionRepositoryAdapter implements UserSessionRepository, UserSessionDetailsRepository {
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
    public PageResult<UserSessionDetails> findAllByProjectId(String projectId, Pagination pagination) {
        Page<UserSessionDetails> page = repository.findAllDetailsByProjectId(
                projectId,
                pageRequest(pagination)
        ).map(this::toDetails);

        return toPageResult(page);
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

    @Override
    public long countByProjectIdAndUserIdAndRevokedAtIsNull(String projectId, String userId) {
        return repository.countByProjectIdAndUserIdAndRevokedAtIsNull(projectId, userId);
    }

    private UserSessionDetails toDetails(UserSessionDetailsProjection projection) {
        return new UserSessionDetails(
                UserSessionMapper.toDomain(projection.getSession()),
                UserMapper.toDomain(projection.getSessionUser())
        );
    }

    private PageRequest pageRequest(Pagination pagination) {
        return PageRequest.of(
                pagination.page(),
                pagination.size(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
    }

    private PageResult<UserSessionDetails> toPageResult(Page<UserSessionDetails> page) {
        return new PageResult<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
