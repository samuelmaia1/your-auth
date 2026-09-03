package com.samuelmaia1_github.yourauth.presentation.mapper;

import com.samuelmaia1_github.yourauth.domain.projectapikey.CreatedProjectApiKey;
import com.samuelmaia1_github.yourauth.domain.projectapikey.ProjectApiKey;
import com.samuelmaia1_github.yourauth.domain.projectapikey.ProjectApiKeyDetails;
import com.samuelmaia1_github.yourauth.domain.shared.PageResult;
import com.samuelmaia1_github.yourauth.presentation.dto.projectapikey.CreateProjectApiKeyDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.projectapikey.CreatedProjectApiKeyResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.projectapikey.ProjectApiKeyDetailsResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.projectapikey.ProjectApiKeyResponseDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;

@Component
public class ProjectApiKeyPresentationMapper {
    public static ProjectApiKey toDomain(CreateProjectApiKeyDTO dto, String projectId) {
        return ProjectApiKey.builder()
                .projectId(projectId)
                .name(dto.name())
                .scopes(dto.scopes() == null ? null : new HashSet<>(dto.scopes()))
                .expiresAt(toExpiresAt(dto.expiresInHours()))
                .build();
    }

    public static CreatedProjectApiKeyResponseDTO toResponseDTO(CreatedProjectApiKey createdApiKey) {
        return new CreatedProjectApiKeyResponseDTO(
                createdApiKey.rawKey(),
                toResponseDTO(createdApiKey.apiKey())
        );
    }

    public static ProjectApiKeyResponseDTO toResponseDTO(ProjectApiKey apiKey) {
        return new ProjectApiKeyResponseDTO(
                apiKey.getId(),
                apiKey.getProjectId(),
                apiKey.getName(),
                apiKey.getKeyId(),
                apiKey.getPrefix(),
                apiKey.getSecretLastFour(),
                apiKey.getEnvironment(),
                apiKey.getScopes() == null ? new HashSet<>() : new HashSet<>(apiKey.getScopes()),
                apiKey.getCreatedByAccountId(),
                apiKey.getCreatedAt(),
                apiKey.getUpdatedAt(),
                apiKey.getLastUsedAt(),
                apiKey.getRevokedAt(),
                apiKey.getExpiresAt()
        );
    }

    public static ProjectApiKeyDetailsResponseDTO toDetailsResponseDTO(ProjectApiKeyDetails details) {
        ProjectApiKey apiKey = details.apiKey();

        return new ProjectApiKeyDetailsResponseDTO(
                apiKey.getId(),
                apiKey.getProjectId(),
                apiKey.getName(),
                apiKey.getKeyId(),
                apiKey.getPrefix(),
                apiKey.getSecretLastFour(),
                apiKey.getEnvironment(),
                apiKey.getScopes() == null ? new HashSet<>() : new HashSet<>(apiKey.getScopes()),
                apiKey.getCreatedByAccountId(),
                AccountPresentationMapper.toBasicResponseDTO(details.createdByAccount()),
                apiKey.getCreatedAt(),
                apiKey.getUpdatedAt(),
                apiKey.getLastUsedAt(),
                apiKey.getRevokedAt(),
                apiKey.getExpiresAt()
        );
    }

    public static PageResult<ProjectApiKeyResponseDTO> toResponseDTO(PageResult<ProjectApiKey> apiKeys) {
        return new PageResult<>(
                apiKeys.content().stream()
                        .map(ProjectApiKeyPresentationMapper::toResponseDTO)
                        .toList(),
                apiKeys.page(),
                apiKeys.size(),
                apiKeys.totalElements(),
                apiKeys.totalPages()
        );
    }

    public static PageResult<ProjectApiKeyDetailsResponseDTO> toDetailsResponseDTO(
            PageResult<ProjectApiKeyDetails> apiKeys
    ) {
        return new PageResult<>(
                apiKeys.content().stream()
                        .map(ProjectApiKeyPresentationMapper::toDetailsResponseDTO)
                        .toList(),
                apiKeys.page(),
                apiKeys.size(),
                apiKeys.totalElements(),
                apiKeys.totalPages()
        );
    }

    private static LocalDateTime toExpiresAt(Long expiresInHours) {
        if (expiresInHours == null) {
            return null;
        }

        return LocalDateTime.now().plusHours(expiresInHours);
    }
}
