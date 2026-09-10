package com.samuelmaia1_github.yourauth.domain.auth;

import com.samuelmaia1_github.yourauth.domain.auth.exceptions.InvalidCredentialsException;
import com.samuelmaia1_github.yourauth.domain.auth.exceptions.InvalidTokenException;
import com.samuelmaia1_github.yourauth.domain.refreshtoken.AccountRefreshTokenService;
import com.samuelmaia1_github.yourauth.domain.account.Account;
import com.samuelmaia1_github.yourauth.domain.account.AccountRepository;
import com.samuelmaia1_github.yourauth.domain.account.exceptions.AccountNotFoundException;
import com.samuelmaia1_github.yourauth.domain.accountsession.AccountSession;
import com.samuelmaia1_github.yourauth.domain.accountsession.AccountSessionRepository;
import com.samuelmaia1_github.yourauth.domain.valueobjects.CPF;
import com.samuelmaia1_github.yourauth.infra.interfaces.IPasswordEncoder;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.AccountLoginSessionDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.AccountRefreshResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.AccountSessionTokensDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.LoginDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.user.TokenDTO;
import com.samuelmaia1_github.yourauth.presentation.mapper.AccountPresentationMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountAuthService {

    private final AccountRepository accountRepository;
    private final AccountSessionRepository accountSessionRepository;
    private final IPasswordEncoder encoder;
    private final TokenService tokenService;
    private final AccountRefreshTokenService accountRefreshTokenService;

    @Transactional
    public AccountLoginSessionDTO login(LoginDTO credentials, String ipAddress, String userAgent, String deviceName) {
        Account account = findAccount(credentials);

        if (!encoder.matches(credentials.password(), account.getPassword())) {
            throw new InvalidCredentialsException("Credenciais inválidas.");
        }

        AccountSession session = AccountSession
                .builder()
                .accountId(account.getId())
                .lastUsedAt(Instant.now())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .deviceName(deviceName)
                .build();

        AccountSession savedSession = accountSessionRepository.save(session);

        TokenDTO refreshToken = accountRefreshTokenService.createAccountRefreshToken(
                account.getId(),
                savedSession.getId(),
                userAgent
        );

        return new AccountLoginSessionDTO(
                AccountPresentationMapper.toResponseDTO(account),
                buildAccessToken(account, savedSession.getId()),
                refreshToken
        );
    }

    @Transactional
    public AccountSessionTokensDTO refreshAccountSession(String rawRefreshToken) {
        AccountRefreshResponseDTO refreshResponse = accountRefreshTokenService.refresh(rawRefreshToken);

        AccountSession session = findValidSessionOrThrow(refreshResponse.accountId(), refreshResponse.sessionId());
        Account account = accountRepository
                .findById(refreshResponse.accountId())
                .orElseThrow(() -> new AccountNotFoundException("Conta não encontrada"));

        session.refresh();
        accountSessionRepository.save(session);

        return new AccountSessionTokensDTO(
                buildAccessToken(account, session.getId()),
                refreshResponse.refreshToken()
        );
    }

    @Transactional
    public void logoutAccountSession(String rawRefreshToken) {
        accountRefreshTokenService.logout(rawRefreshToken);
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

    private AccountSession findValidSessionOrThrow(String accountId, String sessionId) {
        AccountSession session = accountSessionRepository.findById(sessionId)
                .orElse(null);

        if (session == null) {
            accountRefreshTokenService.revokeSession(sessionId);

            throw new InvalidTokenException("Sessão inválida ou expirada.");
        }

        if (!Objects.equals(accountId, session.getAccountId()) || !session.isValid()) {
            accountRefreshTokenService.revokeSession(sessionId);

            throw new InvalidTokenException("Sessão inválida ou expirada.");
        }

        return session;
    }

    private TokenDTO buildAccessToken(Account account, String sessionId) {
        return new TokenDTO(
                tokenService.generateToken(account, sessionId),
                tokenService.getAccessTokenDuration()
        );
    }

}
