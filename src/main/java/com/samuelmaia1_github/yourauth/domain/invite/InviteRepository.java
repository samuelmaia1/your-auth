package com.samuelmaia1_github.yourauth.domain.invite;

import com.samuelmaia1_github.yourauth.domain.shared.PageResult;
import com.samuelmaia1_github.yourauth.domain.shared.Pagination;

import java.util.Optional;

public interface InviteRepository {
    Invite save(Invite invite);

    Optional<Invite> findById(String id);

    PageResult<Invite> findAllByRecipientAccountId(
            String recipientAccountId,
            Pagination pagination,
            InviteStatus status
    );

    PageResult<Invite> findAllByProjectId(
            String projectId,
            Pagination pagination,
            InviteStatus status
    );

    boolean existsByProjectIdAndRecipientAccountIdAndStatus(
            String projectId,
            String recipientAccountId,
            InviteStatus status
    );
}
