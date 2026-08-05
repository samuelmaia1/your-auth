package com.samuelmaia1_github.yourauth.domain.auth;

import com.samuelmaia1_github.yourauth.domain.auth.exceptions.InvalidCredentialsException;
import com.samuelmaia1_github.yourauth.domain.refreshtoken.RefreshToken;
import com.samuelmaia1_github.yourauth.domain.refreshtoken.RefreshTokenService;
import com.samuelmaia1_github.yourauth.domain.account.Account;
import com.samuelmaia1_github.yourauth.domain.account.AccountRepository;
import com.samuelmaia1_github.yourauth.domain.account.exceptions.AccountNotFoundException;
import com.samuelmaia1_github.yourauth.domain.valueobjects.CPF;
import com.samuelmaia1_github.yourauth.infra.interfaces.IPasswordEncoder;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.LoginDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.LoginResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.RefreshResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.TokensResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.mapper.AccountPresentationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AccountRepository accountRepository;
    private final IPasswordEncoder encoder;
    private final TokenService tokenService;
    private final RefreshTokenService refreshTokenService;

    public LoginResponseDTO login(LoginDTO credentials) {
        Account account = findAccount(credentials);

        if (!encoder.matches(credentials.password(), account.getPassword())) {
            throw new InvalidCredentialsException("Credenciais inválidas.");
        }

        return new LoginResponseDTO(
                AccountPresentationMapper.toResponseDTO(account),
                tokenService.generateToken(account)
        );
    }

    public String generateRefreshToken(String accountId, String userAgent) {
        return refreshTokenService.createRefreshToken(accountId, userAgent);
    }

    public TokensResponseDTO refreshSession(String rawRefreshToken) {
        RefreshResponseDTO refreshResponse = refreshTokenService.refresh(rawRefreshToken);

        Account account = accountRepository
                .findById(refreshResponse.accountId())
                .orElseThrow(() -> new AccountNotFoundException("Conta não encontrada"));

        return new TokensResponseDTO(tokenService.generateToken(account), refreshResponse.rawRefreshToken());
    }

    private Account findAccount(LoginDTO credentials) {
        Optional<Account> optionalAccount;

        if (credentials.email() != null && !credentials.email().isBlank()) {
            optionalAccount = accountRepository.findByEmail(credentials.email());
        } else {
            CPF cpf = new CPF(credentials.cpf());
            optionalAccount = accountRepository.findByCPF(cpf);
        }

        return optionalAccount.orElseThrow(
                () -> new InvalidCredentialsException("Credenciais inválidas.")
        );
    }

}
