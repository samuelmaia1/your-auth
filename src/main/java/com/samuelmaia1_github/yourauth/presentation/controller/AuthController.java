package com.samuelmaia1_github.yourauth.presentation.controller;

import com.samuelmaia1_github.yourauth.domain.auth.AccountAuthService;
import com.samuelmaia1_github.yourauth.presentation.dto.account.AccountResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.AccountLoginSessionDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.AccountRefreshRequestDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.AccountSessionTokensDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.AccountTokensResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.LoginDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.LoginMobileResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.user.TokenDTO;
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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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

    public AuthController(AccountAuthService service) {
        this.service = service;
    }

    @PostMapping("/login")
    @Operation(
            summary = "Autentica uma conta proprietaria no fluxo web",
            description = "Valida as credenciais da conta, cria uma sessao persistida e define cookies HTTP-only de access token e refresh token. O access token carrega o sessionId e deixa de autenticar quando a sessao e revogada."
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
                    description = "Identificador do client/dispositivo usado para registrar a sessao e o refresh token.",
                    in = ParameterIn.HEADER,
                    example = "Mozilla/5.0"
            )
            @RequestHeader(
                    value = HttpHeaders.USER_AGENT,
                    required = false
            ) String userAgent,
            @Parameter(
                    description = "IP real da conta autenticada.",
                    in = ParameterIn.HEADER,
                    example = "203.0.113.10"
            )
            @RequestHeader(
                    value = "X-End-User-IP",
                    required = false
            ) String ipAddress,
            @Parameter(
                    description = "Nome amigavel do dispositivo usado na sessao.",
                    in = ParameterIn.HEADER,
                    example = "Chrome macOS"
            )
            @RequestHeader(
                    value = "X-Device-Name",
                    required = false
            ) String deviceName,
            HttpServletRequest request
    ) {
        AccountLoginSessionDTO loginData = service.login(
                loginDTO,
                resolveIpAddress(ipAddress, request),
                userAgent,
                deviceName
        );

        AccountResponseDTO account = loginData.account();

        ResponseCookie refreshCookie = buildRefreshCookie(loginData.refreshToken());
        ResponseCookie accessCookie = buildAccessCookie(loginData.accessToken());

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .body(account);
    }

    @PostMapping("/mobile/login")
    @Operation(
            summary = "Autentica uma conta proprietaria no fluxo mobile",
            description = "Valida as credenciais da conta, cria uma sessao persistida e retorna access token e refresh token no corpo da resposta. O access token carrega o sessionId e deixa de autenticar quando a sessao e revogada."
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
                    description = "Identificador do client/dispositivo usado para registrar a sessao e o refresh token.",
                    in = ParameterIn.HEADER,
                    example = "YourAuthMobile/1.0"
            )
            @RequestHeader(
                    value = HttpHeaders.USER_AGENT,
                    required = false
            ) String userAgent,
            @Parameter(
                    description = "IP real da conta autenticada.",
                    in = ParameterIn.HEADER,
                    example = "203.0.113.10"
            )
            @RequestHeader(
                    value = "X-End-User-IP",
                    required = false
            ) String ipAddress,
            @Parameter(
                    description = "Nome amigavel do dispositivo usado na sessao.",
                    in = ParameterIn.HEADER,
                    example = "YourAuthMobile iOS"
            )
            @RequestHeader(
                    value = "X-Device-Name",
                    required = false
            ) String deviceName,
            HttpServletRequest request
    ) {
        AccountLoginSessionDTO loginData = service.login(
                loginDTO,
                resolveIpAddress(ipAddress, request),
                userAgent,
                deviceName
        );

        AccountResponseDTO account = loginData.account();

        return ResponseEntity
                .ok()
                .body(new LoginMobileResponseDTO(
                        account,
                        loginData.accessToken().raw(),
                        loginData.refreshToken().raw()
                ));
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Renova a sessao web da conta",
            description = "Usa o cookie refresh_token para validar a sessao persistida da conta, rotacionar o refresh token, gerar access token com sessionId e redefinir os cookies HTTP-only.",
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
        AccountSessionTokensDTO tokens = service.refreshAccountSession(refreshToken);

        ResponseCookie refreshCookie = buildRefreshCookie(tokens.refreshToken());
        ResponseCookie accessCookie = buildAccessCookie(tokens.accessToken());

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .body(toAccountTokensResponse(tokens));
    }

    @PostMapping("/mobile/refresh")
    @Operation(
            summary = "Renova a sessao mobile da conta",
            description = "Usa o refresh token enviado no corpo da requisicao para validar a sessao persistida da conta, rotacionar o refresh token e gerar access token com sessionId."
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
        AccountSessionTokensDTO tokens = service.refreshAccountSession(requestDTO.refreshToken());

        return ResponseEntity
                .ok()
                .body(toAccountTokensResponse(tokens));
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Encerra a sessao web da conta",
            description = "Usa o cookie refresh_token para revogar a sessao persistida da conta, invalidando refresh tokens da sessao e access tokens emitidos com o mesmo sessionId.",
            security = @SecurityRequirement(name = "refreshTokenCookie")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Sessao encerrada e cookies limpos.",
                    headers = @Header(
                            name = HttpHeaders.SET_COOKIE,
                            description = "Limpa os cookies HTTP-only access-token e refresh_token.",
                            schema = @Schema(type = "string")
                    ),
                    content = @Content
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
            )
    })
    public ResponseEntity<Void> logout(
            @Parameter(
                    description = "Refresh token HTTP-only recebido no login web.",
                    in = ParameterIn.COOKIE,
                    required = true
            )
            @CookieValue("refresh_token") String refreshToken
    ) {
        service.logoutAccountSession(refreshToken);

        return ResponseEntity
                .noContent()
                .header(HttpHeaders.SET_COOKIE, clearCookie("refresh_token").toString())
                .header(HttpHeaders.SET_COOKIE, clearCookie("access-token").toString())
                .build();
    }

    @PostMapping("/mobile/logout")
    @Operation(
            summary = "Encerra a sessao mobile da conta",
            description = "Usa o refresh token enviado no corpo da requisicao para revogar a sessao persistida da conta, invalidando refresh tokens da sessao e access tokens emitidos com o mesmo sessionId."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Sessao encerrada.",
                    content = @Content
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
            )
    })
    public ResponseEntity<Void> logoutMobile(
            @Valid @RequestBody AccountRefreshRequestDTO requestDTO
    ) {
        service.logoutAccountSession(requestDTO.refreshToken());

        return ResponseEntity.noContent().build();
    }

    private ResponseCookie buildRefreshCookie(TokenDTO refreshToken) {
        return ResponseCookie.from("refresh_token", refreshToken.raw())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("None")
                .maxAge(refreshToken.duration())
                .build();
    }

    private ResponseCookie buildAccessCookie(TokenDTO accessToken) {
        return ResponseCookie.from("access-token", accessToken.raw())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("None")
                .maxAge(accessToken.duration())
                .build();
    }

    private ResponseCookie clearCookie(String name) {
        return ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("None")
                .maxAge(Duration.ZERO)
                .build();
    }

    private AccountTokensResponseDTO toAccountTokensResponse(AccountSessionTokensDTO tokens) {
        return new AccountTokensResponseDTO(tokens.accessToken().raw(), tokens.refreshToken().raw());
    }

    private String resolveIpAddress(String ipAddress, HttpServletRequest request) {
        if (ipAddress != null && !ipAddress.isBlank()) {
            return ipAddress;
        }

        return request.getRemoteAddr();
    }
}
