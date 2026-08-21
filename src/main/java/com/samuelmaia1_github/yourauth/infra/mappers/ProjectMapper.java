package com.samuelmaia1_github.yourauth.infra.mappers;

import com.samuelmaia1_github.yourauth.domain.project.Project;
import com.samuelmaia1_github.yourauth.infra.repository.entity.ProjectEntity;

public class ProjectMapper {
    private ProjectMapper() {
    }

    public static Project toDomain(ProjectEntity entity) {
        if (entity == null) {
            return null;
        }

        return Project.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .ownerAccountId(entity.getOwnerAccountId())
                .status(entity.getStatus())
                .environment(entity.getEnvironment())
                .tokenAudience(entity.getTokenAudience())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static ProjectEntity toEntity(Project project) {
        if (project == null) {
            return null;
        }

        return ProjectEntity.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .ownerAccountId(project.getOwnerAccountId())
                .status(project.getStatus())
                .environment(project.getEnvironment())
                .tokenAudience(project.getTokenAudience())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }
}
