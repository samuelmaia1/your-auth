package com.samuelmaia1_github.yourauth.presentation.controller;

import com.samuelmaia1_github.yourauth.domain.auth.AuthenticatedAccount;
import com.samuelmaia1_github.yourauth.domain.project.passwordconfig.PasswordConfig;
import com.samuelmaia1_github.yourauth.domain.project.passwordconfig.PasswordConfigService;
import com.samuelmaia1_github.yourauth.presentation.dto.error.ErrorResponse;
import com.samuelmaia1_github.yourauth.presentation.dto.passwordconfig.PasswordConfigDTO;
import com.samuelmaia1_github.yourauth.presentation.mapper.PasswordConfigPresentationMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/projects/{projectId}/password-config")
@RequiredArgsConstructor
@Tag(name = "Project Password Config", description = "Configuracoes de senha de um projeto.")
@SecurityRequirement(name = "bearerAuth")
@SecurityRequirement(name = "accessTokenCookie")
public class PasswordConfigController {
    private final PasswordConfigService service;

    @GetMapping
    @Operation(summary = "Busca a configuracao de senha de um projeto")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Configuracao encontrada.",
                    content = @Content(schema = @Schema(implementation = PasswordConfigDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Autenticacao obrigatoria.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Conta sem acesso ao projeto.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Projeto ou configuracao nao encontrados.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<PasswordConfigDTO> getPasswordConfig(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount,
            @PathVariable String projectId
    ) {
        PasswordConfig config = service.findByProjectId(projectId, authenticatedAccount.id());

        return ResponseEntity.ok(PasswordConfigPresentationMapper.toDto(config));
    }

    @PutMapping
    @Operation(summary = "Atualiza a configuracao de senha de um projeto")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Configuracao atualizada.",
                    content = @Content(schema = @Schema(implementation = PasswordConfigDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Corpo da requisicao invalido ou erro de validacao.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Autenticacao obrigatoria.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Conta sem permissao para alterar a configuracao.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Projeto ou configuracao nao encontrados.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<PasswordConfigDTO> update(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount,
            @PathVariable String projectId,
            @Valid @RequestBody PasswordConfigDTO dto
    ) {
        PasswordConfig config = PasswordConfigPresentationMapper.toDomain(dto);
        PasswordConfig updatedConfig = service.update(projectId, config, authenticatedAccount.id());

        return ResponseEntity.ok(PasswordConfigPresentationMapper.toDto(updatedConfig));
    }

    @DeleteMapping
    @Operation(summary = "Remove a configuracao de senha de um projeto")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Configuracao removida."),
            @ApiResponse(
                    responseCode = "401",
                    description = "Autenticacao obrigatoria.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Conta sem permissao para remover a configuracao.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Projeto ou configuracao nao encontrados.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<Void> delete(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount,
            @PathVariable String projectId
    ) {
        service.delete(projectId, authenticatedAccount.id());

        return ResponseEntity.noContent().build();
    }
}
