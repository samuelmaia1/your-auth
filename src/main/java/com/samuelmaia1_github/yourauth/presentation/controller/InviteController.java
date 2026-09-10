package com.samuelmaia1_github.yourauth.presentation.controller;

import com.samuelmaia1_github.yourauth.domain.auth.AuthenticatedAccount;
import com.samuelmaia1_github.yourauth.domain.invite.Invite;
import com.samuelmaia1_github.yourauth.domain.invite.InviteService;
import com.samuelmaia1_github.yourauth.domain.invite.InviteStatus;
import com.samuelmaia1_github.yourauth.domain.shared.PageResult;
import com.samuelmaia1_github.yourauth.domain.shared.Pagination;
import com.samuelmaia1_github.yourauth.presentation.dto.error.ErrorResponse;
import com.samuelmaia1_github.yourauth.presentation.dto.invite.InviteResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.invite.SendInviteDTO;
import com.samuelmaia1_github.yourauth.presentation.mapper.InvitePresentationMapper;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Invites", description = "Convites para participacao em projetos.")
@SecurityRequirement(name = "bearerAuth")
@SecurityRequirement(name = "accessTokenCookie")
public class InviteController {
    private final InviteService service;

    @PostMapping("/projects/{projectId}/invites")
    @Operation(summary = "Envia um convite para participar de um projeto")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Convite enviado.",
                    content = @Content(schema = @Schema(implementation = InviteResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Corpo da requisicao invalido ou role nao permitida.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Autenticacao obrigatoria.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Conta sem permissao para enviar convites neste projeto.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Projeto ou conta destinataria nao encontrados.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conta ja participa do projeto ou ja possui convite pendente.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<InviteResponseDTO> send(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount,
            @PathVariable String projectId,
            @Valid @RequestBody SendInviteDTO dto
    ) {
        Invite invite = service.send(
                projectId,
                authenticatedAccount.id(),
                dto.recipientAccountId(),
                dto.role()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(InvitePresentationMapper.toResponseDTO(invite));
    }

    @GetMapping("/projects/{projectId}/invites")
    @Operation(summary = "Lista convites de um projeto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Convites encontrados."),
            @ApiResponse(
                    responseCode = "400",
                    description = "Parametros de paginacao ou filtros invalidos.",
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
    public ResponseEntity<PageResult<InviteResponseDTO>> findAllByProject(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount,
            @PathVariable String projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) InviteStatus status
    ) {
        PageResult<Invite> invites = service.findAllByProjectId(
                projectId,
                authenticatedAccount.id(),
                new Pagination(page, size),
                status
        );

        return ResponseEntity.ok(InvitePresentationMapper.toResponseDTO(invites));
    }

    @GetMapping("/invites/received")
    @Operation(summary = "Lista convites recebidos pela conta autenticada")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Convites encontrados."),
            @ApiResponse(
                    responseCode = "400",
                    description = "Parametros de paginacao ou filtros invalidos.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Autenticacao obrigatoria.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<PageResult<InviteResponseDTO>> findReceived(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) InviteStatus status
    ) {
        PageResult<Invite> invites = service.findAllReceived(
                authenticatedAccount.id(),
                new Pagination(page, size),
                status
        );

        return ResponseEntity.ok(InvitePresentationMapper.toResponseDTO(invites));
    }

    @PostMapping("/invites/{inviteId}/accept")
    @Operation(summary = "Aceita um convite recebido")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Convite aceito.",
                    content = @Content(schema = @Schema(implementation = InviteResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Autenticacao obrigatoria.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Conta sem permissao para acessar este convite.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Convite nao encontrado.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Convite nao esta pendente ou conta ja participa do projeto.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<InviteResponseDTO> accept(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount,
            @PathVariable String inviteId
    ) {
        Invite invite = service.accept(inviteId, authenticatedAccount.id());

        return ResponseEntity.ok(InvitePresentationMapper.toResponseDTO(invite));
    }

    @PostMapping("/invites/{inviteId}/refuse")
    @Operation(summary = "Recusa um convite recebido")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Convite recusado.",
                    content = @Content(schema = @Schema(implementation = InviteResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Autenticacao obrigatoria.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Conta sem permissao para acessar este convite.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Convite nao encontrado.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Convite nao esta pendente.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<InviteResponseDTO> refuse(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount,
            @PathVariable String inviteId
    ) {
        Invite invite = service.refuse(inviteId, authenticatedAccount.id());

        return ResponseEntity.ok(InvitePresentationMapper.toResponseDTO(invite));
    }
}
