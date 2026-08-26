package com.samuelmaia1_github.yourauth.domain.auth;

import com.samuelmaia1_github.yourauth.domain.auth.exceptions.InvalidCredentialsException;
import com.samuelmaia1_github.yourauth.domain.auth.exceptions.LoginBlockedException;
import com.samuelmaia1_github.yourauth.domain.project.authconfig.AuthConfig;
import com.samuelmaia1_github.yourauth.domain.project.authconfig.AuthConfigRepository;
import com.samuelmaia1_github.yourauth.domain.project.authconfig.exceptions.AuthConfigNotFoundException;
import com.samuelmaia1_github.yourauth.domain.refreshtoken.UserRefreshTokenService;
import com.samuelmaia1_github.yourauth.domain.user.User;
import com.samuelmaia1_github.yourauth.domain.user.UserRepository;
import com.samuelmaia1_github.yourauth.domain.user.exceptions.UserNotFoundException;
import com.samuelmaia1_github.yourauth.domain.usersession.UserSessionRepository;
import com.samuelmaia1_github.yourauth.infra.interfaces.IPasswordEncoder;
import com.samuelmaia1_github.yourauth.infra.utils.Formatter;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.user.TokenDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.user.UserLoginDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.user.UserLoginResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.mapper.UserPresentationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class UserAuthService {
    private final UserRepository userRepository;
    private final AuthConfigRepository authConfigRepository;
    private final UserSessionRepository sessionRepository;
    private final IPasswordEncoder encoder;
    private final TokenService tokenService;

    public UserLoginResponseDTO login(
            UserLoginDTO credentials,
            String projectId,
            String ipAddress,
            String userAgent
    ) {
        User user = userRepository.findByProjectIdAndEmailIgnoreCase(projectId, credentials.email())
                .orElseThrow(() -> new UserNotFoundException("Usuário não cadastrado."));

        AuthConfig authConfig = authConfigRepository.findByProjectId(projectId)
                .orElseThrow(AuthConfigNotFoundException::new);

        try {
            ensureCanLogin(user);
            validateCredentials(credentials.password(), user.getPassword());

            String accessToken = tokenService.generateToken(user, projectId, authConfig);

            Duration accessTokenDuration = Duration.ofMinutes(authConfig.getAccessTokenExpirationMinutes());

            user.recordSuccessfulLogin(ipAddress, userAgent);

            userRepository.save(user);

            return new UserLoginResponseDTO(
                    UserPresentationMapper.toResponseDTO(user),
                    new TokenDTO(accessToken, accessTokenDuration),
                    true,
                    LocalDateTime.now()
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

    private void ensureCanLogin(User user) {
        if (user.isLocked()) {
            String formatted = Formatter.formatLocalDateTime(user.getLockedUntil());

            throw new LoginBlockedException("Usuário bloquado de fazer login até: " + formatted);
        }

    }

    private void validateCredentials(String password, String hashedPassword) {
        if (!encoder.matches(password, hashedPassword))
            throw new InvalidCredentialsException("Credenciais inválidas.");
    }
}
