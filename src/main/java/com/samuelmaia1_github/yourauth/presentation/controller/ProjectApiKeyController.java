package com.samuelmaia1_github.yourauth.presentation.controller;

import com.samuelmaia1_github.yourauth.domain.auth.AuthenticatedAccount;
import com.samuelmaia1_github.yourauth.domain.projectapikey.CreatedProjectApiKey;
import com.samuelmaia1_github.yourauth.domain.projectapikey.ProjectApiKey;
import com.samuelmaia1_github.yourauth.domain.projectapikey.ProjectApiKeyService;
import com.samuelmaia1_github.yourauth.domain.shared.PageResult;
import com.samuelmaia1_github.yourauth.domain.shared.Pagination;
import com.samuelmaia1_github.yourauth.presentation.dto.projectapikey.CreateProjectApiKeyDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.projectapikey.CreatedProjectApiKeyResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.projectapikey.ProjectApiKeyResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.mapper.ProjectApiKeyPresentationMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/projects/{projectId}/api-keys")
public class ProjectApiKeyController {
    private final ProjectApiKeyService service;

    @PostMapping
    public ResponseEntity<CreatedProjectApiKeyResponseDTO> create(
            @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount,
            @PathVariable String projectId,
            @Valid @RequestBody CreateProjectApiKeyDTO dto
    ) {
        ProjectApiKey apiKey = ProjectApiKeyPresentationMapper.toDomain(dto, projectId);
        CreatedProjectApiKey createdApiKey = service.create(apiKey, authenticatedAccount.id());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ProjectApiKeyPresentationMapper.toResponseDTO(createdApiKey));
    }

    @GetMapping
    public ResponseEntity<PageResult<ProjectApiKeyResponseDTO>> findAll(
            @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount,
            @PathVariable String projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageResult<ProjectApiKey> apiKeys = service.findAllByProjectId(
                projectId,
                authenticatedAccount.id(),
                new Pagination(page, size)
        );

        return ResponseEntity.ok(ProjectApiKeyPresentationMapper.toResponseDTO(apiKeys));
    }

    @GetMapping("/{apiKeyId}")
    public ResponseEntity<ProjectApiKeyResponseDTO> findById(
            @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount,
            @PathVariable String projectId,
            @PathVariable String apiKeyId
    ) {
        ProjectApiKey apiKey = service.findById(projectId, apiKeyId, authenticatedAccount.id());

        return ResponseEntity.ok(ProjectApiKeyPresentationMapper.toResponseDTO(apiKey));
    }

    @PostMapping("/{apiKeyId}/revoke")
    public ResponseEntity<ProjectApiKeyResponseDTO> revoke(
            @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount,
            @PathVariable String projectId,
            @PathVariable String apiKeyId
    ) {
        ProjectApiKey revokedApiKey = service.revoke(projectId, apiKeyId, authenticatedAccount.id());

        return ResponseEntity.ok(ProjectApiKeyPresentationMapper.toResponseDTO(revokedApiKey));
    }

    @DeleteMapping("/{apiKeyId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount,
            @PathVariable String projectId,
            @PathVariable String apiKeyId
    ) {
        service.delete(projectId, apiKeyId, authenticatedAccount.id());

        return ResponseEntity.noContent().build();
    }
}
