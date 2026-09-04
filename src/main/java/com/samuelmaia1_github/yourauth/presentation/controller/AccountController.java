package com.samuelmaia1_github.yourauth.presentation.controller;

import com.samuelmaia1_github.yourauth.domain.account.Account;
import com.samuelmaia1_github.yourauth.domain.account.AccountService;
import com.samuelmaia1_github.yourauth.domain.account.AccountSummary;
import com.samuelmaia1_github.yourauth.domain.account.AccountSummaryService;
import com.samuelmaia1_github.yourauth.domain.auth.AuthenticatedAccount;
import com.samuelmaia1_github.yourauth.domain.auth.exceptions.InvalidTokenException;
import com.samuelmaia1_github.yourauth.domain.subscription.AccountSubscription;
import com.samuelmaia1_github.yourauth.domain.subscription.AccountSubscriptionService;
import com.samuelmaia1_github.yourauth.presentation.dto.account.AccountResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.account.AccountSummaryResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.account.CreateAccountDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.error.ErrorResponse;
import com.samuelmaia1_github.yourauth.presentation.dto.subscription.AccountSubscriptionResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.mapper.AccountPresentationMapper;
import com.samuelmaia1_github.yourauth.presentation.mapper.AccountSubscriptionPresentationMapper;
import com.samuelmaia1_github.yourauth.presentation.mapper.AccountSummaryPresentationMapper;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Cadastro de contas proprietarias do Your Auth.")
public class AccountController {
    private final AccountService service;
    private final AccountSummaryService summaryService;
    private final AccountSubscriptionService subscriptionService;

    @GetMapping("/me")
    @Operation(
            summary = "Busca os dados da conta autenticada",
            description = "Retorna os dados da conta proprietaria associada ao token de acesso.",
            security = {
                    @SecurityRequirement(name = "bearerAuth"),
                    @SecurityRequirement(name = "accessTokenCookie")
            }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Conta autenticada encontrada.",
                    content = @Content(schema = @Schema(implementation = AccountResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Autenticacao obrigatoria ou token de conta invalido.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Conta nao encontrada.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<AccountResponseDTO> me(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount
    ) {
        AuthenticatedAccount currentAccount = requireAuthenticatedAccount(authenticatedAccount);
        Account account = service.findByIdOrEmail(currentAccount.id(), currentAccount.email());

        return ResponseEntity.ok(AccountPresentationMapper.toResponseDTO(account));
    }

    @GetMapping("/me/summary")
    @Operation(
            summary = "Busca o resumo da conta autenticada",
            description = "Retorna os projetos em que a conta autenticada e membro, com role e metricas resumidas.",
            security = {
                    @SecurityRequirement(name = "bearerAuth"),
                    @SecurityRequirement(name = "accessTokenCookie")
            }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Resumo da conta autenticada encontrado.",
                    content = @Content(schema = @Schema(implementation = AccountSummaryResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Autenticacao obrigatoria ou token de conta invalido.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Conta nao encontrada.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<AccountSummaryResponseDTO> summary(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount
    ) {
        AuthenticatedAccount currentAccount = requireAuthenticatedAccount(authenticatedAccount);
        Account account = service.findByIdOrEmail(currentAccount.id(), currentAccount.email());
        AccountSummary summary = summaryService.findByAccountId(account.getId());

        return ResponseEntity.ok(AccountSummaryPresentationMapper.toResponseDTO(summary));
    }

    @GetMapping("/me/subscription")
    @Operation(
            summary = "Busca a assinatura atual da conta autenticada",
            description = "Retorna o plano e o estado da assinatura atual da conta proprietaria autenticada.",
            security = {
                    @SecurityRequirement(name = "bearerAuth"),
                    @SecurityRequirement(name = "accessTokenCookie")
            }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Assinatura atual encontrada.",
                    content = @Content(schema = @Schema(implementation = AccountSubscriptionResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Autenticacao obrigatoria ou token de conta invalido.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Conta ou assinatura nao encontrada.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<AccountSubscriptionResponseDTO> subscription(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount
    ) {
        AuthenticatedAccount currentAccount = requireAuthenticatedAccount(authenticatedAccount);
        Account account = service.findByIdOrEmail(currentAccount.id(), currentAccount.email());
        AccountSubscription subscription = subscriptionService.findCurrentByAccountId(account.getId());

        return ResponseEntity.ok(AccountSubscriptionPresentationMapper.toResponseDTO(subscription));
    }

    @PostMapping("/create")
    @Operation(
            summary = "Cria uma conta proprietaria",
            description = "Cadastra uma conta que podera criar e gerenciar projetos no Your Auth."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Conta criada com sucesso.",
                    content = @Content(schema = @Schema(implementation = AccountResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Corpo da requisicao invalido ou erro de validacao.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Ja existe conta com o e-mail ou CPF informado.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<AccountResponseDTO> create(@Valid @RequestBody CreateAccountDTO dto) {
        Account account = AccountPresentationMapper.toDomain(dto);
        Account createdAccount = service.create(account);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(AccountPresentationMapper.toResponseDTO(createdAccount));
    }

    private AuthenticatedAccount requireAuthenticatedAccount(AuthenticatedAccount authenticatedAccount) {
        if (authenticatedAccount == null) {
            throw new InvalidTokenException("Token de conta inválido.");
        }

        return authenticatedAccount;
    }
}
