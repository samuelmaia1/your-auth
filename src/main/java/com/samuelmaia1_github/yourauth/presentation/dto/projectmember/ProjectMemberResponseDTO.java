package com.samuelmaia1_github.yourauth.presentation.dto.projectmember;

import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMemberRole;

import java.time.LocalDateTime;

public record ProjectMemberResponseDTO(
        String name,
        String lastName,
        ProjectMemberRole role,
        LocalDateTime joinedAt
) {
}
