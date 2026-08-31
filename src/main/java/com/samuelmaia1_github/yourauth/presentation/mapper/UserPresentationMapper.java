package com.samuelmaia1_github.yourauth.presentation.mapper;

import com.samuelmaia1_github.yourauth.domain.shared.PageResult;
import com.samuelmaia1_github.yourauth.domain.user.User;
import com.samuelmaia1_github.yourauth.domain.user.UserStatus;
import com.samuelmaia1_github.yourauth.presentation.dto.user.CreateUserDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.user.UpdateUserDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.user.UserResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class UserPresentationMapper {
    public static User toDomain(CreateUserDTO dto, String projectId) {
        return User.builder()
                .projectId(projectId)
                .email(dto.email())
                .password(dto.password())
                .status(UserStatus.ACTIVE)
                .phone(PhonePresentationMapper.toDomain(dto.phone()))
                .build();
    }

    public static User toDomain(UpdateUserDTO dto) {
        return User.builder()
                .email(dto.email())
                .password(dto.password())
                .phone(PhonePresentationMapper.toDomain(dto.phone()))
                .build();
    }

    public static UserResponseDTO toResponseDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getProjectId(),
                user.getEmail(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getLastLoginAt(),
                user.getLastPasswordChangedAt(),
                user.getLastFailedLoginAt(),
                user.getFailedLoginAttempts(),
                user.getLockedUntil(),
                user.getLastLoginIpAddress(),
                user.getLastLoginUserAgent(),
                PhonePresentationMapper.toDTO(user.getPhone())
        );
    }

    public static PageResult<UserResponseDTO> toResponseDTO(PageResult<User> users) {
        return new PageResult<>(
                users.content().stream()
                        .map(UserPresentationMapper::toResponseDTO)
                        .toList(),
                users.page(),
                users.size(),
                users.totalElements(),
                users.totalPages()
        );
    }
}
