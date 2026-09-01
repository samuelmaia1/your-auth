package com.samuelmaia1_github.yourauth.presentation.controller;

import com.samuelmaia1_github.yourauth.domain.auth.AccountAuthService;
import com.samuelmaia1_github.yourauth.presentation.dto.account.AccountResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.AccountRefreshRequestDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.AccountTokensResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.LoginDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.LoginMobileResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.LoginResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.error.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/auth")
@Tag(name = "Account Authentication", description = "Login e renovacao de sessao para contas proprietarias.")
public class AuthController {

    private final AccountAuthService service;
    private final Duration refreshTokenDuration;
    private final Duration accessTokenDuration;

    public AuthController(
            AccountAuthService service,
            @Value("${api.security.account-refresh-token.duration:${api.security.refresh-token.duration}}")
            Duration refreshTokenDuration,
            @Value("${api.security.access-token.duration}") Duration accessTokenDuration
    ) {
        this.service = service;
        this.refreshTokenDuration = refreshTokenDuration;
        this.accessTokenDuration = accessTokenDuration;
    }

    @PostMapping("/login")
    @Operation(
            summary = "Autentica uma conta proprietaria no fluxo web",
            description = "Valida as credenciais da conta, cria uma sessao e define cookies HTTP-only de access token e refresh token."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Conta autenticada. Os cookies access-token e refresh_token sao enviados no header Set-Cookie.",
                    headers = @Header(
                            name = HttpHeaders.SET_COOKIE,
                            description = "Define os cookies HTTP-only access-token e refresh_token.",
                            schema = @Schema(type = "string")
                    ),
                    content = @Content(schema = @Schema(implementation = AccountResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Corpo da requisicao invalido ou erro de validacao.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Credenciais invalidas ou login bloqueado.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Falha ao gerar tokens da sessao.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<AccountResponseDTO> login(
            @Valid @RequestBody LoginDTO loginDTO,
            @Parameter(
                    description = "Identificador do client/dispositivo usado para registrar o refresh token.",
                    in = ParameterIn.HEADER,
                    example = "Mozilla/5.0"
            )
            @RequestHeader(
                    value = HttpHeaders.USER_AGENT,
                    required = false
            ) String userAgent
    ) {
        LoginResponseDTO loginData = service.login(loginDTO);

        AccountResponseDTO account = loginData.account();
        String accessToken = loginData.token();

        String rawRefreshToken = service.generateAccountRefreshToken(account.id(), userAgent);

        ResponseCookie refreshCookie = buildRefreshCookie(rawRefreshToken);
        ResponseCookie accessCookie = buildAccessCookie(accessToken);

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .body(account);
    }

    @PostMapping("/mobile/login")
    @Operation(
            summary = "Autentica uma conta proprietaria no fluxo mobile",
            description = "Valida as credenciais da conta e retorna access token e refresh token no corpo da resposta."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Conta autenticada com tokens retornados no body.",
                    content = @Content(schema = @Schema(implementation = LoginMobileResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Corpo da requisicao invalido ou erro de validacao.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Credenciais invalidas ou login bloqueado.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Falha ao gerar tokens da sessao.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<LoginMobileResponseDTO> mobileLogin(
            @Valid @RequestBody LoginDTO loginDTO,
            @Parameter(
                    description = "Identificador do client/dispositivo usado para registrar o refresh token.",
                    in = ParameterIn.HEADER,
                    example = "YourAuthMobile/1.0"
            )
            @RequestHeader(
                    value = HttpHeaders.USER_AGENT,
                    required = false
            ) String userAgent
    ) {
        LoginResponseDTO loginData = service.login(loginDTO);

        AccountResponseDTO account = loginData.account();
        String accessToken = loginData.token();

        String rawRefreshToken = service.generateAccountRefreshToken(account.id(), userAgent);

        return ResponseEntity
                .ok()
                .body(new LoginMobileResponseDTO(account, accessToken, rawRefreshToken));
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Renova a sessao web da conta",
            description = "Usa o cookie refresh_token para gerar novos tokens e redefinir os cookies HTTP-only.",
            security = @SecurityRequirement(name = "refreshTokenCookie")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Sessao renovada. Novos cookies access-token e refresh_token sao enviados no header Set-Cookie.",
                    headers = @Header(
                            name = HttpHeaders.SET_COOKIE,
                            description = "Define novos cookies HTTP-only access-token e refresh_token.",
                            schema = @Schema(type = "string")
                    ),
                    content = @Content(schema = @Schema(implementation = AccountTokensResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Cookie refresh_token ausente.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Refresh token invalido, expirado ou reutilizado.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Conta associada ao refresh token nao encontrada.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Falha ao validar, armazenar ou gerar tokens.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<AccountTokensResponseDTO> refreshToken(
            @Parameter(
                    description = "Refresh token HTTP-only recebido no login web.",
                    in = ParameterIn.COOKIE,
                    required = true
            )
            @CookieValue("refresh_token") String refreshToken
    ) {
        AccountTokensResponseDTO tokens = service.refreshAccountSession(refreshToken);

        ResponseCookie refreshCookie = buildRefreshCookie(tokens.refreshToken());
        ResponseCookie accessCookie = buildAccessCookie(tokens.accessToken());

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .body(tokens);
    }

    @PostMapping("/mobile/refresh")
    @Operation(
            summary = "Renova a sessao mobile da conta",
            description = "Usa o refresh token enviado no corpo da requisicao para gerar um novo par de tokens."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Sessao renovada com access token e refresh token retornados no body.",
                    content = @Content(schema = @Schema(implementation = AccountTokensResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Corpo da requisicao invalido ou erro de validacao.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Refresh token invalido, expirado ou reutilizado.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Conta associada ao refresh token nao encontrada.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Falha ao validar, armazenar ou gerar tokens.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<AccountTokensResponseDTO> refreshMobileToken(
            @Valid @RequestBody AccountRefreshRequestDTO requestDTO
    ) {
        AccountTokensResponseDTO tokens = service.refreshAccountSession(requestDTO.refreshToken());

        return ResponseEntity
                .ok()
                .body(tokens);
    }

    private ResponseCookie buildRefreshCookie(String refreshToken) {
        return ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("None")
                .maxAge(refreshTokenDuration)
                .build();
    }

    private ResponseCookie buildAccessCookie(String accessToken) {
        return ResponseCookie.from("access-token", accessToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("None")
                .maxAge(accessTokenDuration)
                .build();
    }
}
