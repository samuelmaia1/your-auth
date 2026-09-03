package com.samuelmaia1_github.yourauth.domain.auth;

import com.samuelmaia1_github.yourauth.domain.auth.exceptions.InvalidCredentialsException;
import com.samuelmaia1_github.yourauth.domain.auth.exceptions.InvalidTokenException;
import com.samuelmaia1_github.yourauth.domain.auth.exceptions.LoginBlockedException;
import com.samuelmaia1_github.yourauth.domain.project.authconfig.AuthConfig;
import com.samuelmaia1_github.yourauth.domain.project.authconfig.AuthConfigRepository;
import com.samuelmaia1_github.yourauth.domain.project.authconfig.exceptions.AuthConfigNotFoundException;
import com.samuelmaia1_github.yourauth.domain.refreshtoken.UserRefreshTokenService;
import com.samuelmaia1_github.yourauth.domain.user.User;
import com.samuelmaia1_github.yourauth.domain.user.UserRepository;
import com.samuelmaia1_github.yourauth.domain.user.exceptions.UserNotFoundException;
import com.samuelmaia1_github.yourauth.domain.usersession.UserSession;
import com.samuelmaia1_github.yourauth.domain.usersession.UserSessionPolicy;
import com.samuelmaia1_github.yourauth.domain.usersession.UserSessionRepository;
import com.samuelmaia1_github.yourauth.infra.interfaces.IPasswordEncoder;
import com.samuelmaia1_github.yourauth.infra.utils.Formatter;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.user.TokenDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.user.UserLoginDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.user.UserLoginResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.user.UserLoginSessionDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.user.UserRefreshResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.user.UserTokensResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.mapper.UserPresentationMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserAuthService {
    private final UserRepository userRepository;
    private final AuthConfigRepository authConfigRepository;
    private final UserSessionRepository sessionRepository;
    private final UserSessionPolicy userSessionPolicy;
    private final IPasswordEncoder encoder;
    private final TokenService tokenService;
    private final UserRefreshTokenService refreshTokenService;

    @Transactional
    public UserLoginSessionDTO login(
            UserLoginDTO credentials,
            String projectId,
            String ipAddress,
            String userAgent,
            String deviceName
    ) {
        User user = userRepository.findByProjectIdAndEmailIgnoreCase(projectId, credentials.email())
                .orElseThrow(() -> new UserNotFoundException("Usuário não cadastrado."));

        AuthConfig authConfig = findAuthConfigOrThrow(projectId);

        try {
            ensureCanLogin(user, authConfig);
            validateCredentials(credentials.password(), user.getPassword());

            UserSession session = UserSession
                    .builder()
                    .userId(user.getId())
                    .lastUsedAt(Instant.now())
                    .projectId(projectId)
                    .deviceName(deviceName)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .build();

            UserSession savedSession = sessionRepository.save(session);

            TokenDTO accessToken = buildAccessToken(user, projectId, authConfig);
            TokenDTO refreshToken = refreshTokenService.createUserRefreshToken(
                    projectId,
                    user.getId(),
                    savedSession.getId(),
                    userAgent
            );

            user.recordSuccessfulLogin(ipAddress, userAgent);

            userRepository.save(user);

            return new UserLoginSessionDTO(
                    new UserLoginResponseDTO(
                            UserPresentationMapper.toResponseDTO(user),
                            accessToken,
                            true,
                            LocalDateTime.now()
                    ),
                    refreshToken
            );
        } catch (InvalidCredentialsException exception) {
            user.recordFailedLogin();

            if (user.getFailedLoginAttempts() > authConfig.getFailedLoginAttemptsLimit()) {
                user.lockUntil(LocalDateTime.now().plusMinutes(authConfig.getLockDurationMinutes()));
            }

            userRepository.save(user);

            throw new InvalidCredentialsException(exception.getMessage());
        }
    }

    @Transactional
    public UserTokensResponseDTO refreshUserSession(String rawRefreshToken, String authenticatedProjectId) {
        UserRefreshResponseDTO refreshResponse = refreshTokenService.refresh(rawRefreshToken, authenticatedProjectId);

        AuthConfig authConfig = findAuthConfigOrThrow(refreshResponse.projectId());
        UserSession session = findValidSessionOrThrow(refreshResponse);
        User user = userRepository
                .findByProjectIdAndId(refreshResponse.projectId(), refreshResponse.userId())
                .orElseThrow(() -> new UserNotFoundException("Usuário não cadastrado."));

        session.refresh();
        sessionRepository.save(session);

        return new UserTokensResponseDTO(
                buildAccessToken(user, refreshResponse.projectId(), authConfig),
                refreshResponse.refreshToken()
        );
    }

    @Transactional
    public void logoutUserSession(String rawRefreshToken, String authenticatedProjectId) {
        refreshTokenService.logout(rawRefreshToken, authenticatedProjectId);
    }

    private void ensureCanLogin(User user, AuthConfig authConfig) {
        userSessionPolicy.ensureMaxSessionsAreNotExceeded(authConfig, authConfig.getProjectId(), user.getId());

        if (user.isLocked()) {
            String formatted = Formatter.formatLocalDateTime(user.getLockedUntil());

            throw new LoginBlockedException("Usuário bloquado de fazer login até: " + formatted);
        }

    }

    private void validateCredentials(String password, String hashedPassword) {
        if (!encoder.matches(password, hashedPassword))
            throw new InvalidCredentialsException("Credenciais inválidas.");
    }

    private AuthConfig findAuthConfigOrThrow(String projectId) {
        return authConfigRepository.findByProjectId(projectId)
                .orElseThrow(AuthConfigNotFoundException::new);
    }

    private UserSession findValidSessionOrThrow(UserRefreshResponseDTO refreshResponse) {
        UserSession session = sessionRepository.findById(refreshResponse.sessionId())
                .orElse(null);

        if (session == null) {
            refreshTokenService.revokeSession(refreshResponse.sessionId());

            throw new InvalidTokenException("Sessão inválida ou expirada.");
        }

        boolean isSameUserSession = refreshResponse.projectId().equals(session.getProjectId())
                && refreshResponse.userId().equals(session.getUserId());

        if (!isSameUserSession || !session.isValid()) {
            refreshTokenService.revokeSession(refreshResponse.sessionId());

            throw new InvalidTokenException("Sessão inválida ou expirada.");
        }

        return session;
    }

    private TokenDTO buildAccessToken(User user, String projectId, AuthConfig authConfig) {
        String accessToken = tokenService.generateToken(user, projectId, authConfig);
        Duration accessTokenDuration = Duration.ofMinutes(authConfig.getAccessTokenExpirationMinutes());

        return new TokenDTO(accessToken, accessTokenDuration);
    }
}
