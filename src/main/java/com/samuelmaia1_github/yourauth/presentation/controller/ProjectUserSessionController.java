package com.samuelmaia1_github.yourauth.presentation.controller;

import com.samuelmaia1_github.yourauth.domain.auth.AuthenticatedAccount;
import com.samuelmaia1_github.yourauth.domain.shared.PageResult;
import com.samuelmaia1_github.yourauth.domain.shared.Pagination;
import com.samuelmaia1_github.yourauth.domain.usersession.UserSessionDetails;
import com.samuelmaia1_github.yourauth.domain.usersession.UserSessionService;
import com.samuelmaia1_github.yourauth.presentation.dto.error.ErrorResponse;
import com.samuelmaia1_github.yourauth.presentation.dto.usersession.UserSessionResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.mapper.UserSessionPresentationMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/projects/{projectId}/sessions")
@Tag(name = "Project User Sessions", description = "Consulta administrativa de sessoes de usuarios finais.")
@SecurityRequirement(name = "bearerAuth")
@SecurityRequirement(name = "accessTokenCookie")
public class ProjectUserSessionController {
    private final UserSessionService service;

    @GetMapping
    @Operation(summary = "Lista sessoes de usuarios finais de um projeto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sessoes encontradas."),
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
    public ResponseEntity<PageResult<UserSessionResponseDTO>> findAll(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount,
            @PathVariable String projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageResult<UserSessionDetails> sessions = service.findAllByProjectId(
                projectId,
                authenticatedAccount.id(),
                new Pagination(page, size)
        );

        return ResponseEntity.ok(UserSessionPresentationMapper.toResponseDTO(sessions));
    }
}
