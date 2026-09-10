package com.samuelmaia1_github.yourauth.presentation.dto.invite;

import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMemberRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SendInviteDTO(
        @NotBlank
        String recipientAccountId,

        @NotNull
        ProjectMemberRole role
) {
}
