package com.samuelmaia1_github.yourauth.presentation.controller;

import com.samuelmaia1_github.yourauth.domain.auth.AuthenticatedAccount;
import com.samuelmaia1_github.yourauth.domain.projectapikey.CreatedProjectApiKey;
import com.samuelmaia1_github.yourauth.domain.projectapikey.ProjectApiKey;
import com.samuelmaia1_github.yourauth.domain.projectapikey.ProjectApiKeyDetails;
import com.samuelmaia1_github.yourauth.domain.projectapikey.ProjectApiKeyService;
import com.samuelmaia1_github.yourauth.domain.shared.PageResult;
import com.samuelmaia1_github.yourauth.domain.shared.Pagination;
import com.samuelmaia1_github.yourauth.presentation.dto.projectapikey.CreateProjectApiKeyDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.projectapikey.CreatedProjectApiKeyResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.projectapikey.ProjectApiKeyDetailsResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.projectapikey.ProjectApiKeyResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.error.ErrorResponse;
import com.samuelmaia1_github.yourauth.presentation.mapper.ProjectApiKeyPresentationMapper;
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
@Tag(name = "Project API Keys", description = "Gestao de API keys usadas por clientes terceiros.")
@SecurityRequirement(name = "bearerAuth")
@SecurityRequirement(name = "accessTokenCookie")
public class ProjectApiKeyController {
    private final ProjectApiKeyService service;

    @PostMapping
    @Operation(
            summary = "Cria uma API key de projeto",
            description = "Retorna a chave bruta apenas na resposta de criacao. Responses futuras exibem somente metadados."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "API key criada.",
                    content = @Content(schema = @Schema(implementation = CreatedProjectApiKeyResponseDTO.class))
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
                    description = "Conta sem acesso ao projeto.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Projeto nao encontrado.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<CreatedProjectApiKeyResponseDTO> create(
            @Parameter(hidden = true)
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
    @Operation(summary = "Lista API keys de um projeto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "API keys encontradas."),
            @ApiResponse(
                    responseCode = "400",
                    description = "Parametros de paginacao invalidos.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
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
                    description = "Projeto nao encontrado.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<PageResult<ProjectApiKeyDetailsResponseDTO>> findAll(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount,
            @PathVariable String projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageResult<ProjectApiKeyDetails> apiKeys = service.findAllByProjectId(
                projectId,
                authenticatedAccount.id(),
                new Pagination(page, size)
        );

        return ResponseEntity.ok(ProjectApiKeyPresentationMapper.toDetailsResponseDTO(apiKeys));
    }

    @GetMapping("/{apiKeyId}")
    @Operation(summary = "Busca uma API key de projeto pelo id")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "API key encontrada.",
                    content = @Content(schema = @Schema(implementation = ProjectApiKeyDetailsResponseDTO.class))
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
                    description = "Projeto ou API key nao encontrados.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<ProjectApiKeyDetailsResponseDTO> findById(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount,
            @PathVariable String projectId,
            @PathVariable String apiKeyId
    ) {
        ProjectApiKeyDetails apiKey = service.findById(projectId, apiKeyId, authenticatedAccount.id());

        return ResponseEntity.ok(ProjectApiKeyPresentationMapper.toDetailsResponseDTO(apiKey));
    }

    @PostMapping("/{apiKeyId}/revoke")
    @Operation(summary = "Revoga uma API key de projeto")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "API key revogada.",
                    content = @Content(schema = @Schema(implementation = ProjectApiKeyResponseDTO.class))
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
                    description = "Projeto ou API key nao encontrados.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "API key ja estava revogada.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<ProjectApiKeyResponseDTO> revoke(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount,
            @PathVariable String projectId,
            @PathVariable String apiKeyId
    ) {
        ProjectApiKey revokedApiKey = service.revoke(projectId, apiKeyId, authenticatedAccount.id());

        return ResponseEntity.ok(ProjectApiKeyPresentationMapper.toResponseDTO(revokedApiKey));
    }

    @DeleteMapping("/{apiKeyId}")
    @Operation(summary = "Remove uma API key de projeto")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "API key removida."),
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
                    description = "Projeto ou API key nao encontrados.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<Void> delete(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount,
            @PathVariable String projectId,
            @PathVariable String apiKeyId
    ) {
        service.delete(projectId, apiKeyId, authenticatedAccount.id());

        return ResponseEntity.noContent().build();
    }
}
