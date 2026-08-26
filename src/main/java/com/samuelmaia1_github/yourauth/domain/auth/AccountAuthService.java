package com.samuelmaia1_github.yourauth.domain.auth;

import com.samuelmaia1_github.yourauth.domain.auth.exceptions.InvalidCredentialsException;
import com.samuelmaia1_github.yourauth.domain.refreshtoken.AccountRefreshTokenService;
import com.samuelmaia1_github.yourauth.domain.account.Account;
import com.samuelmaia1_github.yourauth.domain.account.AccountRepository;
import com.samuelmaia1_github.yourauth.domain.account.exceptions.AccountNotFoundException;
import com.samuelmaia1_github.yourauth.domain.valueobjects.CPF;
import com.samuelmaia1_github.yourauth.infra.interfaces.IPasswordEncoder;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.AccountRefreshResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.AccountTokensResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.LoginDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.LoginResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.mapper.AccountPresentationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountAuthService {

    private final AccountRepository accountRepository;
    private final IPasswordEncoder encoder;
    private final TokenService tokenService;
    private final AccountRefreshTokenService accountRefreshTokenService;

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

    public String generateAccountRefreshToken(String accountId, String userAgent) {
        return accountRefreshTokenService.createAccountRefreshToken(accountId, userAgent);
    }

    public AccountTokensResponseDTO refreshAccountSession(String rawRefreshToken) {
        AccountRefreshResponseDTO refreshResponse = accountRefreshTokenService.refresh(rawRefreshToken);

        Account account = accountRepository
                .findById(refreshResponse.accountId())
                .orElseThrow(() -> new AccountNotFoundException("Conta não encontrada"));

        return new AccountTokensResponseDTO(tokenService.generateToken(account), refreshResponse.rawRefreshToken());
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
