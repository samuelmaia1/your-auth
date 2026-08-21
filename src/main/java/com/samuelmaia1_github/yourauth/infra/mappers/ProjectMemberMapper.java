package com.samuelmaia1_github.yourauth.infra.mappers;

import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMember;
import com.samuelmaia1_github.yourauth.infra.repository.entity.ProjectMemberEntity;

public class ProjectMemberMapper {
    private ProjectMemberMapper() {
    }

    public static ProjectMember toDomain(ProjectMemberEntity entity) {
        if (entity == null) {
            return null;
        }

        return ProjectMember.builder()
                .id(entity.getId())
                .projectId(entity.getProjectId())
                .accountId(entity.getAccountId())
                .role(entity.getRole())
                .joinedAt(entity.getJoinedAt())
                .build();
    }

    public static ProjectMemberEntity toEntity(ProjectMember projectMember) {
        if (projectMember == null) {
            return null;
        }

        return ProjectMemberEntity.builder()
                .id(projectMember.getId())
                .projectId(projectMember.getProjectId())
                .accountId(projectMember.getAccountId())
                .role(projectMember.getRole())
                .joinedAt(projectMember.getJoinedAt())
                .build();
    }
}
