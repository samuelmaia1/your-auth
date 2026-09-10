package com.samuelmaia1_github.yourauth.presentation.dto.invite;

import com.samuelmaia1_github.yourauth.domain.invite.InviteStatus;
import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMemberRole;

import java.time.LocalDateTime;

public record InviteResponseDTO(
        String id,
        String senderAccountId,
        String senderAccountEmail,
        String recipientAccountId,
        String recipientAccountEmail,
        ProjectMemberRole role,
        InviteStatus status,
        LocalDateTime sentAt,
        String projectId,
        String projectName,
        String projectDescription
) {
}
