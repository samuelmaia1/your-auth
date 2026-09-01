package com.samuelmaia1_github.yourauth.presentation.controller;

import com.samuelmaia1_github.yourauth.domain.account.Account;
import com.samuelmaia1_github.yourauth.domain.account.AccountService;
import com.samuelmaia1_github.yourauth.presentation.dto.account.CreateAccountDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.account.AccountResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.error.ErrorResponse;
import com.samuelmaia1_github.yourauth.presentation.mapper.AccountPresentationMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Cadastro de contas proprietarias do Your Auth.")
public class AccountController {
    private final AccountService service;

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
}
