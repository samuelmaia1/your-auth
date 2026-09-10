package com.samuelmaia1_github.yourauth.infra.repository;

import com.samuelmaia1_github.yourauth.domain.invite.InviteStatus;
import com.samuelmaia1_github.yourauth.infra.repository.entity.InviteEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InviteJpaRepository extends JpaRepository<InviteEntity, String> {
    Page<InviteEntity> findAllByRecipientAccountId(String recipientAccountId, Pageable pageable);

    Page<InviteEntity> findAllByRecipientAccountIdAndStatus(
            String recipientAccountId,
            InviteStatus status,
            Pageable pageable
    );

    Page<InviteEntity> findAllByProjectId(String projectId, Pageable pageable);

    Page<InviteEntity> findAllByProjectIdAndStatus(
            String projectId,
            InviteStatus status,
            Pageable pageable
    );

    boolean existsByProjectIdAndRecipientAccountIdAndStatus(
            String projectId,
            String recipientAccountId,
            InviteStatus status
    );
}
