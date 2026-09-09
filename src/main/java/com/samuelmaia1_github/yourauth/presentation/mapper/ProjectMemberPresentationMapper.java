package com.samuelmaia1_github.yourauth.presentation.mapper;

import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMemberDetails;
import com.samuelmaia1_github.yourauth.domain.shared.PageResult;
import com.samuelmaia1_github.yourauth.presentation.dto.projectmember.ProjectMemberResponseDTO;

public class ProjectMemberPresentationMapper {
    private ProjectMemberPresentationMapper() {
    }

    public static ProjectMemberResponseDTO toResponseDTO(ProjectMemberDetails details) {
        return new ProjectMemberResponseDTO(
                details.accountId(),
                details.name(),
                details.lastName(),
                details.role(),
                details.joinedAt()
        );
    }

    public static PageResult<ProjectMemberResponseDTO> toResponseDTO(PageResult<ProjectMemberDetails> members) {
        return new PageResult<>(
                members.content().stream()
                        .map(ProjectMemberPresentationMapper::toResponseDTO)
                        .toList(),
                members.page(),
                members.size(),
                members.totalElements(),
                members.totalPages()
        );
    }
}
