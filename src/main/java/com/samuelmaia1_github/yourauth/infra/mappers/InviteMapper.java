package com.samuelmaia1_github.yourauth.infra.mappers;

import com.samuelmaia1_github.yourauth.domain.invite.Invite;
import com.samuelmaia1_github.yourauth.infra.repository.entity.InviteEntity;

public class InviteMapper {
    private InviteMapper() {
    }

    public static Invite toDomain(InviteEntity entity) {
        if (entity == null) {
            return null;
        }

        return Invite.builder()
                .id(entity.getId())
                .senderAccountId(entity.getSenderAccountId())
                .senderAccountEmail(entity.getSenderAccountEmail())
                .recipientAccountId(entity.getRecipientAccountId())
                .recipientAccountEmail(entity.getRecipientAccountEmail())
                .role(entity.getRole())
                .status(entity.getStatus())
                .sentAt(entity.getSentAt())
                .projectId(entity.getProjectId())
                .projectName(entity.getProjectName())
                .projectDescription(entity.getProjectDescription())
                .build();
    }

    public static InviteEntity toEntity(Invite invite) {
        if (invite == null) {
            return null;
        }

        return InviteEntity.builder()
                .id(invite.getId())
                .senderAccountId(invite.getSenderAccountId())
                .senderAccountEmail(invite.getSenderAccountEmail())
                .recipientAccountId(invite.getRecipientAccountId())
                .recipientAccountEmail(invite.getRecipientAccountEmail())
                .role(invite.getRole())
                .status(invite.getStatus())
                .sentAt(invite.getSentAt())
                .projectId(invite.getProjectId())
                .projectName(invite.getProjectName())
                .projectDescription(invite.getProjectDescription())
                .build();
    }
}
