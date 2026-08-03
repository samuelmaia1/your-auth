package com.samuelmaia1_github.yourauth.domain.auth;

import com.samuelmaia1_github.yourauth.domain.auth.exceptions.InvalidCredentialsException;
import com.samuelmaia1_github.yourauth.domain.user.User;
import com.samuelmaia1_github.yourauth.domain.user.UserRepository;
import com.samuelmaia1_github.yourauth.domain.valueobjects.CPF;
import com.samuelmaia1_github.yourauth.infra.interfaces.IPasswordEncoder;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.LoginDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.LoginResponseDTO;
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
