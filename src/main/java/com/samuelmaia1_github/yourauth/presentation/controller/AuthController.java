package com.samuelmaia1_github.yourauth.presentation.controller;

import com.samuelmaia1_github.yourauth.domain.auth.AccountAuthService;
import com.samuelmaia1_github.yourauth.domain.social.SocialLoginService;
import com.samuelmaia1_github.yourauth.presentation.dto.account.AccountResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.AccountLoginSessionDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.AccountRefreshRequestDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.AccountSessionTokensDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.AccountTokensResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.LoginDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.LoginMobileResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.SocialLoginExchangeRequest;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

import static com.samuelmaia1_github.yourauth.domain.shared.SafeLog.compact;
import static com.samuelmaia1_github.yourauth.domain.shared.SafeLog.maskCpf;
import static com.samuelmaia1_github.yourauth.domain.shared.SafeLog.maskEmail;
import static com.samuelmaia1_github.yourauth.domain.shared.SafeLog.present;

@RestController
@RequestMapping("/auth")
@Tag(name = "Account Authentication", description = "Login e renovacao de sessao para contas proprietarias.")
@Slf4j
public class AuthController {

    private final AccountAuthService service;
    private final SocialLoginService socialLoginService;

    public AuthController(AccountAuthService service, SocialLoginService socialLoginService) {
        this.service = service;
        this.socialLoginService = socialLoginService;
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
        String resolvedIpAddress = resolveIpAddress(ipAddress, request);
        log.info(
                "Requisicao de login web recebida: credential={}, ipAddress={}, userAgent={}, deviceName={}",
                credential(loginDTO),
                compact(resolvedIpAddress, 64),
                compact(userAgent, 120),
                compact(deviceName, 80)
        );

        AccountLoginSessionDTO loginData = service.login(
                loginDTO,
                resolvedIpAddress,
                userAgent,
                deviceName
        );

        AccountResponseDTO account = loginData.account();

        ResponseCookie refreshCookie = buildRefreshCookie(loginData.refreshToken());
        ResponseCookie accessCookie = buildAccessCookie(loginData.accessToken());
        log.info(
                "Login web concluido: accountId={}, email={}, refreshCookieMaxAge={}, accessCookieMaxAge={}",
                account.id(),
                maskEmail(account.email()),
                refreshCookie.getMaxAge(),
                accessCookie.getMaxAge()
        );
        log.debug("Cookies de login web preparados: refreshCookieName=refresh_token, accessCookieName=access-token, sameSite=None, secure=true, httpOnly=true");

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
        String resolvedIpAddress = resolveIpAddress(ipAddress, request);
        log.info(
                "Requisicao de login mobile recebida: credential={}, ipAddress={}, userAgent={}, deviceName={}",
                credential(loginDTO),
                compact(resolvedIpAddress, 64),
                compact(userAgent, 120),
                compact(deviceName, 80)
        );

        AccountLoginSessionDTO loginData = service.login(
                loginDTO,
                resolvedIpAddress,
                userAgent,
                deviceName
        );

        AccountResponseDTO account = loginData.account();
        log.info(
                "Login mobile concluido: accountId={}, email={}, accessTokenDuration={}, refreshTokenDuration={}",
                account.id(),
                maskEmail(account.email()),
                loginData.accessToken().duration(),
                loginData.refreshToken().duration()
        );

        return ResponseEntity
                .ok()
                .body(new LoginMobileResponseDTO(
                        account,
                        loginData.accessToken().raw(),
                        loginData.refreshToken().raw()
                ));
    }

    @PostMapping("/social/exchange")
    @Operation(
            summary = "Troca codigo temporario de login social",
            description = "Valida o codigo de uso unico criado apos OAuth2, cria uma sessao persistida de conta e define cookies HTTP-only de access token e refresh token."
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
                    description = "Codigo social invalido, expirado ou ja consumido.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Falha ao gerar tokens da sessao.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<AccountResponseDTO> exchangeSocialCode(
            @Valid @RequestBody SocialLoginExchangeRequest exchangeRequest,
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
        String resolvedIpAddress = resolveIpAddress(ipAddress, request);
        log.info(
                "Requisicao de troca de codigo social recebida: codePresent={}, ipAddress={}, userAgent={}, deviceName={}",
                present(exchangeRequest.code()),
                compact(resolvedIpAddress, 64),
                compact(userAgent, 120),
                compact(deviceName, 80)
        );

        AccountLoginSessionDTO loginData = socialLoginService.exchangeCode(
                exchangeRequest.code(),
                resolvedIpAddress,
                userAgent,
                deviceName
        );

        ResponseCookie refreshCookie = buildRefreshCookie(loginData.refreshToken());
        ResponseCookie accessCookie = buildAccessCookie(loginData.accessToken());
        log.info(
                "Troca de codigo social concluida: accountId={}, email={}, refreshCookieMaxAge={}, accessCookieMaxAge={}",
                loginData.account().id(),
                maskEmail(loginData.account().email()),
                refreshCookie.getMaxAge(),
                accessCookie.getMaxAge()
        );
        log.debug("Cookies de troca social preparados: refreshCookieName=refresh_token, accessCookieName=access-token, sameSite=None, secure=true, httpOnly=true");

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .body(loginData.account());
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
        log.info("Requisicao de refresh web recebida: refreshTokenPresent={}", present(refreshToken));
        AccountSessionTokensDTO tokens = service.refreshAccountSession(refreshToken);

        ResponseCookie refreshCookie = buildRefreshCookie(tokens.refreshToken());
        ResponseCookie accessCookie = buildAccessCookie(tokens.accessToken());
        log.info(
                "Refresh web concluido: refreshCookieMaxAge={}, accessCookieMaxAge={}",
                refreshCookie.getMaxAge(),
                accessCookie.getMaxAge()
        );
        log.debug("Cookies de refresh web preparados: refreshCookieName=refresh_token, accessCookieName=access-token, sameSite=None, secure=true, httpOnly=true");

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
        log.info("Requisicao de refresh mobile recebida: refreshTokenPresent={}", present(requestDTO.refreshToken()));
        AccountSessionTokensDTO tokens = service.refreshAccountSession(requestDTO.refreshToken());
        log.info(
                "Refresh mobile concluido: accessTokenDuration={}, refreshTokenDuration={}",
                tokens.accessToken().duration(),
                tokens.refreshToken().duration()
        );

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
        log.info("Requisicao de logout web recebida: refreshTokenPresent={}", present(refreshToken));
        service.logoutAccountSession(refreshToken);
        log.info("Logout web concluido: cookiesCleared=true");

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
        log.info("Requisicao de logout mobile recebida: refreshTokenPresent={}", present(requestDTO.refreshToken()));
        service.logoutAccountSession(requestDTO.refreshToken());
        log.info("Logout mobile concluido");

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
            log.debug("IP da autenticacao resolvido pelo header X-End-User-IP: ipAddress={}", compact(ipAddress, 64));
            return ipAddress;
        }

        String remoteAddress = request.getRemoteAddr();
        log.debug("IP da autenticacao resolvido pelo remoteAddr: ipAddress={}", compact(remoteAddress, 64));
        return remoteAddress;
    }

    private String credential(LoginDTO loginDTO) {
        if (loginDTO.email() != null && !loginDTO.email().isBlank()) {
            return "email=" + maskEmail(loginDTO.email());
        }

        return "cpf=" + maskCpf(loginDTO.cpf());
    }
}
