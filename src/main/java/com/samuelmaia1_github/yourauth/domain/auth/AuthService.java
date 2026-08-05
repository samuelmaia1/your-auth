package com.samuelmaia1_github.yourauth.domain.auth;

import com.samuelmaia1_github.yourauth.domain.auth.exceptions.InvalidCredentialsException;
import com.samuelmaia1_github.yourauth.domain.refreshtoken.RefreshToken;
import com.samuelmaia1_github.yourauth.domain.refreshtoken.RefreshTokenService;
import com.samuelmaia1_github.yourauth.domain.user.User;
import com.samuelmaia1_github.yourauth.domain.user.UserRepository;
import com.samuelmaia1_github.yourauth.domain.user.exceptions.UserNotFoundException;
import com.samuelmaia1_github.yourauth.domain.valueobjects.CPF;
import com.samuelmaia1_github.yourauth.infra.interfaces.IPasswordEncoder;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.LoginDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.LoginResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.RefreshResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.TokensResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.mapper.UserPresentationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final IPasswordEncoder encoder;
    private final TokenService tokenService;
    private final RefreshTokenService refreshTokenService;

    public LoginResponseDTO login(LoginDTO credentials) {
        User user = findUser(credentials);

        if (!encoder.matches(credentials.password(), user.getPassword())) {
            throw new InvalidCredentialsException("Credenciais inválidas.");
        }

        return new LoginResponseDTO(
                UserPresentationMapper.toResponseDTO(user),
                tokenService.generateToken(user)
        );
    }

    public String generateRefreshToken(String userId, String userAgent) {
        return refreshTokenService.createRefreshToken(userId, userAgent);
    }

    public TokensResponseDTO refreshSession(String rawRefreshToken) {
        RefreshResponseDTO refreshResponse = refreshTokenService.refresh(rawRefreshToken);

        User user = userRepository
                .findById(refreshResponse.userId())
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));

        return new TokensResponseDTO(tokenService.generateToken(user), refreshResponse.rawRefreshToken());
    }

    private User findUser(LoginDTO credentials) {
        Optional<User> optionalUser;

        if (credentials.email() != null && !credentials.email().isBlank()) {
            optionalUser = userRepository.findByEmail(credentials.email());
        } else {
            CPF cpf = new CPF(credentials.cpf());
            optionalUser = userRepository.findByCPF(cpf);
        }

        return optionalUser.orElseThrow(
                () -> new InvalidCredentialsException("Credenciais inválidas.")
        );
    }

}
