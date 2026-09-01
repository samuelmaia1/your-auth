package com.samuelmaia1_github.yourauth.presentation.controller;

import com.samuelmaia1_github.yourauth.domain.auth.AuthenticatedProjectApiKey;
import com.samuelmaia1_github.yourauth.domain.auth.UserAuthService;
import com.samuelmaia1_github.yourauth.domain.user.User;
import com.samuelmaia1_github.yourauth.domain.user.UserService;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.user.TokenDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.user.UserLoginDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.user.UserLoginResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.user.UserLoginSessionDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.user.UserTokensResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.user.CreateUserDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.user.UserResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.error.ErrorResponse;
import com.samuelmaia1_github.yourauth.presentation.mapper.UserPresentationMapper;
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
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
@Tag(name = "Public Project Users", description = "Endpoints consumidos por aplicacoes terceiras usando API key de projeto.")
public class UserController {
    private final UserService userService;
    private final UserAuthService userAuthService;

    @PostMapping
    @Operation(
            summary = "Cria um usuario final via API key",
            description = "Cria um usuario final no projeto associado a API key enviada.",
            security = @SecurityRequirement(name = "projectApiKey")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Usuario criado.",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Corpo da requisicao invalido, erro de validacao ou API key mal formatada.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "API key ausente, invalida, expirada ou revogada.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "API key sem permissao para criar usuarios.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Usuario ja existente no projeto.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Falha ao processar API key do projeto.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<UserResponseDTO> create(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedProjectApiKey authenticatedApiKey,
            @Valid @RequestBody CreateUserDTO dto
    ) {
        User user = UserPresentationMapper.toDomain(dto, authenticatedApiKey.projectId());
        User createdUser = userService.createWithApiKey(user, authenticatedApiKey.scopes());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(UserPresentationMapper.toResponseDTO(createdUser));
    }

    @PostMapping("/login")
    @Operation(
            summary = "Autentica um usuario final via API key",
            description = "Valida credenciais no projeto associado a API key e define cookies HTTP-only de access token e refresh token.",
            security = @SecurityRequirement(name = "projectApiKey")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario autenticado. Os cookies access-token e refresh_token sao enviados no header Set-Cookie.",
                    headers = @Header(
                            name = HttpHeaders.SET_COOKIE,
                            description = "Define os cookies HTTP-only access-token e refresh_token.",
                            schema = @Schema(type = "string")
                    ),
                    content = @Content(schema = @Schema(implementation = UserLoginResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Corpo da requisicao invalido, erro de validacao ou API key mal formatada.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Credenciais invalidas, login bloqueado ou API key invalida.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "API key sem permissao para autenticar usuarios.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Limite de sessoes ativas atingido.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Falha ao gerar tokens ou processar API key do projeto.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<UserLoginResponseDTO> login(
            @Valid @RequestBody UserLoginDTO credentials,
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedProjectApiKey authenticatedApiKey,
            @Parameter(
                    description = "User agent real do usuario final repassado pela aplicacao cliente.",
                    in = ParameterIn.HEADER,
                    example = "Mozilla/5.0"
            )
            @RequestHeader(
                    value = "X-Forwarded-User-Agent",
                    required = false
            ) String userAgent,
            @Parameter(
                    description = "IP real do usuario final repassado pela aplicacao cliente.",
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
            ) String deviceName
    ) {
        UserLoginSessionDTO loginSession = userAuthService.
                login(credentials, authenticatedApiKey.projectId(), ipAddress, userAgent, deviceName);

        UserLoginResponseDTO responseDTO = loginSession.response();
        TokenDTO accessToken = responseDTO.token();

        TokenDTO refreshToken = loginSession.refreshToken();

        ResponseCookie refreshCookie = buildRefreshCookie(refreshToken);
        ResponseCookie accessCookie = buildAccessCookie(accessToken);

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .body(responseDTO);
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Renova a sessao web de um usuario final",
            description = "Usa o cookie refresh_token para gerar novos tokens e redefinir cookies HTTP-only.",
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
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario ou sessao associados ao refresh token nao encontrados.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Falha ao validar, armazenar ou gerar tokens.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<Void> refresh(
            @Parameter(
                    description = "Refresh token HTTP-only recebido no login do usuario final.",
                    in = ParameterIn.COOKIE,
                    required = true
            )
            @CookieValue("refresh_token") String refreshToken
    ) {
        UserTokensResponseDTO tokens = userAuthService.refreshUserSession(refreshToken);

        ResponseCookie refreshCookie = buildRefreshCookie(tokens.refreshToken());
        ResponseCookie accessCookie = buildAccessCookie(tokens.accessToken());

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .build();
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Encerra a sessao web de um usuario final",
            description = "Revoga a sessao associada ao refresh token e limpa os cookies da sessao.",
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
                    description = "Refresh token HTTP-only recebido no login do usuario final.",
                    in = ParameterIn.COOKIE,
                    required = true
            )
            @CookieValue("refresh_token") String refreshToken
    ) {
        userAuthService.logoutUserSession(refreshToken);

        return ResponseEntity
                .noContent()
                .header(HttpHeaders.SET_COOKIE, clearCookie("refresh_token").toString())
                .header(HttpHeaders.SET_COOKIE, clearCookie("access-token").toString())
                .build();
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
}
