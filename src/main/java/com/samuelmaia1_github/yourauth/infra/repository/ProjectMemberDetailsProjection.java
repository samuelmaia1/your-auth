package com.samuelmaia1_github.yourauth.infra.repository;

import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMemberRole;

import java.time.LocalDateTime;

public record ProjectMemberDetailsProjection(
        String accountId,
        String name,
        String lastName,
        ProjectMemberRole role,
        LocalDateTime joinedAt
) {
}
