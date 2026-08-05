package com.samuelmaia1_github.yourauth.domain.projectmember;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public class ProjectMember {
    private String id;
    private String projectId;
    private String accountId;
    private ProjectMemberRole role;
    private LocalDateTime joinedAt;
}
