package com.samuelmaia1_github.yourauth.presentation.mapper;

import com.samuelmaia1_github.yourauth.domain.shared.PageResult;
import com.samuelmaia1_github.yourauth.domain.project.Project;
import com.samuelmaia1_github.yourauth.domain.project.ProjectStatus;
import com.samuelmaia1_github.yourauth.domain.project.ProjectUpdate;
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

    public static ProjectUpdate toUpdate(UpdateProjectDTO dto) {
        return new ProjectUpdate(
                dto.name(),
                dto.nameProvided(),
                dto.description(),
                dto.descriptionProvided(),
                dto.status(),
                dto.statusProvided(),
                dto.environment(),
                dto.environmentProvided(),
                dto.tokenAudience(),
                dto.tokenAudienceProvided()
        );
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
