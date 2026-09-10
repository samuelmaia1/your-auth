package com.samuelmaia1_github.yourauth.infra.repository.adapter;

import com.samuelmaia1_github.yourauth.domain.invite.Invite;
import com.samuelmaia1_github.yourauth.domain.invite.InviteRepository;
import com.samuelmaia1_github.yourauth.domain.invite.InviteStatus;
import com.samuelmaia1_github.yourauth.domain.shared.PageResult;
import com.samuelmaia1_github.yourauth.domain.shared.Pagination;
import com.samuelmaia1_github.yourauth.infra.mappers.InviteMapper;
import com.samuelmaia1_github.yourauth.infra.repository.InviteJpaRepository;
import com.samuelmaia1_github.yourauth.infra.repository.entity.InviteEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class InviteRepositoryAdapter implements InviteRepository {
    private final InviteJpaRepository repository;

    @Override
    public Invite save(Invite invite) {
        return InviteMapper.toDomain(repository.save(InviteMapper.toEntity(invite)));
    }

    @Override
    public Optional<Invite> findById(String id) {
        return repository.findById(id).map(InviteMapper::toDomain);
    }

    @Override
    public PageResult<Invite> findAllByRecipientAccountId(
            String recipientAccountId,
            Pagination pagination,
            InviteStatus status
    ) {
        Page<Invite> page = findAllByRecipientAccountId(recipientAccountId, status, pageRequest(pagination))
                .map(InviteMapper::toDomain);

        return toPageResult(page);
    }

    @Override
    public PageResult<Invite> findAllByProjectId(
            String projectId,
            Pagination pagination,
            InviteStatus status
    ) {
        Page<Invite> page = findAllByProjectId(projectId, status, pageRequest(pagination))
                .map(InviteMapper::toDomain);

        return toPageResult(page);
    }

    @Override
    public boolean existsByProjectIdAndRecipientAccountIdAndStatus(
            String projectId,
            String recipientAccountId,
            InviteStatus status
    ) {
        return repository.existsByProjectIdAndRecipientAccountIdAndStatus(projectId, recipientAccountId, status);
    }

    private PageRequest pageRequest(Pagination pagination) {
        return PageRequest.of(
                pagination.page(),
                pagination.size(),
                Sort.by(Sort.Direction.DESC, "sentAt")
        );
    }

    private Page<InviteEntity> findAllByRecipientAccountId(
            String recipientAccountId,
            InviteStatus status,
            PageRequest pageRequest
    ) {
        if (status == null) {
            return repository.findAllByRecipientAccountId(recipientAccountId, pageRequest);
        }

        return repository.findAllByRecipientAccountIdAndStatus(recipientAccountId, status, pageRequest);
    }

    private Page<InviteEntity> findAllByProjectId(
            String projectId,
            InviteStatus status,
            PageRequest pageRequest
    ) {
        if (status == null) {
            return repository.findAllByProjectId(projectId, pageRequest);
        }

        return repository.findAllByProjectIdAndStatus(projectId, status, pageRequest);
    }

    private PageResult<Invite> toPageResult(Page<Invite> page) {
        return new PageResult<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
