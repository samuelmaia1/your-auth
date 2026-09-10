package com.samuelmaia1_github.yourauth.presentation.mapper;

import com.samuelmaia1_github.yourauth.domain.invite.Invite;
import com.samuelmaia1_github.yourauth.domain.shared.PageResult;
import com.samuelmaia1_github.yourauth.presentation.dto.invite.InviteResponseDTO;

public class InvitePresentationMapper {
    private InvitePresentationMapper() {
    }

    public static InviteResponseDTO toResponseDTO(Invite invite) {
        return new InviteResponseDTO(
                invite.getId(),
                invite.getSenderAccountId(),
                invite.getSenderAccountEmail(),
                invite.getRecipientAccountId(),
                invite.getRecipientAccountEmail(),
                invite.getRole(),
                invite.getStatus(),
                invite.getSentAt(),
                invite.getProjectId(),
                invite.getProjectName(),
                invite.getProjectDescription()
        );
    }

    public static PageResult<InviteResponseDTO> toResponseDTO(PageResult<Invite> invites) {
        return new PageResult<>(
                invites.content().stream()
                        .map(InvitePresentationMapper::toResponseDTO)
                        .toList(),
                invites.page(),
                invites.size(),
                invites.totalElements(),
                invites.totalPages()
        );
    }
}
