package com.samuelmaia1_github.yourauth.presentation.mapper;

import com.samuelmaia1_github.yourauth.domain.shared.PageResult;
import com.samuelmaia1_github.yourauth.domain.usersession.UserSession;
import com.samuelmaia1_github.yourauth.domain.usersession.UserSessionDetails;
import com.samuelmaia1_github.yourauth.domain.usersession.UserSessionStatus;
import com.samuelmaia1_github.yourauth.presentation.dto.usersession.UserSessionResponseDTO;

public class UserSessionPresentationMapper {
    private UserSessionPresentationMapper() {
    }

    public static UserSessionResponseDTO toResponseDTO(UserSessionDetails details) {
        UserSession session = details.session();

        return new UserSessionResponseDTO(
                session.getId(),
                session.getProjectId(),
                session.getUserId(),
                UserPresentationMapper.toResponseDTO(details.user()),
                session.getDeviceName(),
                session.getIpAddress(),
                session.getUserAgent(),
                session.getCreatedAt(),
                session.getLastUsedAt(),
                session.getRevokedAt(),
                session.isValid() ? UserSessionStatus.ACTIVE : UserSessionStatus.INACTIVE
        );
    }

    public static PageResult<UserSessionResponseDTO> toResponseDTO(PageResult<UserSessionDetails> sessions) {
        return new PageResult<>(
                sessions.content().stream()
                        .map(UserSessionPresentationMapper::toResponseDTO)
                        .toList(),
                sessions.page(),
                sessions.size(),
                sessions.totalElements(),
                sessions.totalPages()
        );
    }
}
