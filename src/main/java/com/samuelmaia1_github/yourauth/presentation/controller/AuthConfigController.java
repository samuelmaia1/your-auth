package com.samuelmaia1_github.yourauth.presentation.controller;

import com.samuelmaia1_github.yourauth.domain.auth.AuthenticatedAccount;
import com.samuelmaia1_github.yourauth.domain.project.authconfig.AuthConfig;
import com.samuelmaia1_github.yourauth.domain.project.authconfig.AuthConfigService;
import com.samuelmaia1_github.yourauth.presentation.dto.authconfig.AuthConfigDTO;
import com.samuelmaia1_github.yourauth.presentation.mapper.AuthConfigPresentationMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/projects/{projectId}/auth-config")
@RequiredArgsConstructor
public class AuthConfigController {
    private final AuthConfigService service;

    @GetMapping
    public ResponseEntity<AuthConfigDTO> getAuthConfig(
            @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount,
            @PathVariable String projectId
    ) {
        AuthConfig config = service.findByProjectId(projectId, authenticatedAccount.id());

        return ResponseEntity.ok(AuthConfigPresentationMapper.toDto(config));
    }

    @PutMapping
    public ResponseEntity<AuthConfigDTO> update(
            @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount,
            @PathVariable String projectId,
            @Valid @RequestBody AuthConfigDTO dto
    ) {
        AuthConfig config = AuthConfigPresentationMapper.toDomain(dto);
        AuthConfig updatedConfig = service.update(projectId, config, authenticatedAccount.id());

        return ResponseEntity.ok(AuthConfigPresentationMapper.toDto(updatedConfig));
    }
}
