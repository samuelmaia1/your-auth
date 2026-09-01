package com.samuelmaia1_github.yourauth.presentation.controller;

import com.samuelmaia1_github.yourauth.domain.auth.AuthenticatedAccount;
import com.samuelmaia1_github.yourauth.domain.project.Project;
import com.samuelmaia1_github.yourauth.domain.project.ProjectService;
import com.samuelmaia1_github.yourauth.domain.project.authconfig.AuthConfig;
import com.samuelmaia1_github.yourauth.domain.project.passwordconfig.PasswordConfig;
import com.samuelmaia1_github.yourauth.domain.project.passwordconfig.PasswordConfigService;
import com.samuelmaia1_github.yourauth.domain.shared.PageResult;
import com.samuelmaia1_github.yourauth.domain.shared.Pagination;
import com.samuelmaia1_github.yourauth.presentation.dto.passwordconfig.PasswordConfigDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.project.CreateProjectDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.project.ProjectResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.project.UpdateProjectDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.error.ErrorResponse;
import com.samuelmaia1_github.yourauth.presentation.mapper.PasswordConfigPresentationMapper;
import com.samuelmaia1_github.yourauth.presentation.mapper.ProjectPresentationMapper;
import com.samuelmaia1_github.yourauth.presentation.mapper.AuthConfigPresentationMapper;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
@Tag(name = "Projects", description = "Gestao de projetos de uma conta proprietaria.")
@SecurityRequirement(name = "bearerAuth")
@SecurityRequirement(name = "accessTokenCookie")
public class ProjectController {
    private final ProjectService service;
    private final PasswordConfigService passwordConfigService;

    @PostMapping("/create")
    @Operation(summary = "Cria um projeto")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Projeto criado com sucesso.",
                    content = @Content(schema = @Schema(implementation = ProjectResponseDTO.class))
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
                    responseCode = "409",
                    description = "Ja existe projeto conflitante para a conta.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<ProjectResponseDTO> create(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount,
            @Valid @RequestBody CreateProjectDTO dto
    ) {
        Project project = ProjectPresentationMapper.toDomain(dto, authenticatedAccount.id());
        PasswordConfig passwordConfig = PasswordConfigPresentationMapper.toDomain(dto.passwordConfig());
        AuthConfig authConfig = AuthConfigPresentationMapper.toDomain(dto.authConfig());

        Project createdProject = service
                .create(
                        project,
                        passwordConfig,
                        authConfig
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ProjectPresentationMapper.toResponseDTO(createdProject));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um projeto pelo id")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Projeto encontrado.",
                    content = @Content(schema = @Schema(implementation = ProjectResponseDTO.class))
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
    public ResponseEntity<ProjectResponseDTO> findById(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount,
            @PathVariable String id
    ) {
        Project project = service.findById(id, authenticatedAccount.id());

        return ResponseEntity.ok(ProjectPresentationMapper.toResponseDTO(project));
    }

    @GetMapping("/{id}/password-config")
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
                    responseCode = "404",
                    description = "Projeto ou configuracao nao encontrados.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<PasswordConfigDTO> getPasswordConfigById(@PathVariable String id) {
        PasswordConfig config = passwordConfigService.findByProjectId(id);

        return ResponseEntity
                .ok(PasswordConfigPresentationMapper.toDto(config));
    }

    @GetMapping
    @Operation(summary = "Lista projetos da conta autenticada")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Projetos encontrados."),
            @ApiResponse(
                    responseCode = "400",
                    description = "Parametros de paginacao invalidos.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Autenticacao obrigatoria.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<PageResult<ProjectResponseDTO>> findAll(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageResult<Project> projects = service.findAllByAccountId(
                authenticatedAccount.id(),
                new Pagination(page, size)
        );

        return ResponseEntity.ok(ProjectPresentationMapper.toResponseDTO(projects));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um projeto")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Projeto atualizado.",
                    content = @Content(schema = @Schema(implementation = ProjectResponseDTO.class))
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
    public ResponseEntity<ProjectResponseDTO> update(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount,
            @PathVariable String id,
            @Valid @RequestBody UpdateProjectDTO dto
    ) {
        Project project = ProjectPresentationMapper.toDomain(dto);
        Project updatedProject = service.update(id, project, authenticatedAccount.id());

        return ResponseEntity.ok(ProjectPresentationMapper.toResponseDTO(updatedProject));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um projeto")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Projeto removido."),
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
    public ResponseEntity<Void> delete(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount,
            @PathVariable String id
    ) {
        service.delete(id, authenticatedAccount.id());

        return ResponseEntity.noContent().build();
    }
}
