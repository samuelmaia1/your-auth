package com.samuelmaia1_github.yourauth.domain.projectmember;

import java.time.LocalDateTime;

public record ProjectMemberDetails(
        String accountId,
        String name,
        String lastName,
        ProjectMemberRole role,
        LocalDateTime joinedAt
) {
}
