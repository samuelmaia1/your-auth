package com.samuelmaia1_github.yourauth.presentation.mapper;

import com.samuelmaia1_github.yourauth.domain.shared.PageResult;
import com.samuelmaia1_github.yourauth.domain.project.Project;
import com.samuelmaia1_github.yourauth.domain.project.ProjectStatus;
import com.samuelmaia1_github.yourauth.presentation.dto.project.CreateProjectDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.project.ProjectResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.project.UpdateProjectDTO;
import org.springframework.stereotype.Component;

@Component
public class ProjectPresentationMapper {
    public static Project toDomain(CreateProjectDTO dto, String ownerAccountId) {
        return Project.builder()
                .name(dto.name())
                .description(dto.description())
                .ownerAccountId(ownerAccountId)
                .status(ProjectStatus.ACTIVE)
                .environment(dto.environment())
                .tokenAudience(dto.tokenAudience())
                .build();
    }

    public static Project toDomain(UpdateProjectDTO dto) {
        return Project.builder()
                .name(dto.name())
                .description(dto.description())
                .status(dto.status())
                .environment(dto.environment())
                .tokenAudience(dto.tokenAudience())
                .build();
    }

    public static ProjectResponseDTO toResponseDTO(Project project) {
        return new ProjectResponseDTO(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getOwnerAccountId(),
                project.getStatus(),
                project.getEnvironment(),
                project.getTokenAudience(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }

    public static PageResult<ProjectResponseDTO> toResponseDTO(PageResult<Project> projects) {
        return new PageResult<>(
                projects.content().stream()
                        .map(ProjectPresentationMapper::toResponseDTO)
                        .toList(),
                projects.page(),
                projects.size(),
                projects.totalElements(),
                projects.totalPages()
        );
    }
}
