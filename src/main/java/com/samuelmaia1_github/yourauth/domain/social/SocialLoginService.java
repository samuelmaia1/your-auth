package com.samuelmaia1_github.yourauth.domain.social;

import com.samuelmaia1_github.yourauth.domain.account.Account;
import com.samuelmaia1_github.yourauth.domain.account.AccountRepository;
import com.samuelmaia1_github.yourauth.domain.account.AccountService;
import com.samuelmaia1_github.yourauth.domain.auth.AccountAuthService;
import com.samuelmaia1_github.yourauth.domain.auth.exceptions.InvalidTokenException;
import com.samuelmaia1_github.yourauth.domain.refreshtoken.RefreshTokenHasher;
import com.samuelmaia1_github.yourauth.domain.social.exceptions.SocialIdentityConflictException;
import com.samuelmaia1_github.yourauth.domain.social.exceptions.SocialLoginException;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.AccountLoginSessionDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SocialLoginService {
    private static final Duration CODE_DURATION = Duration.ofSeconds(45);

    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final SocialIdentityRepository identityRepository;
    private final SocialLoginCodeRepository codeRepository;
    private final SocialLoginCodeGenerator codeGenerator;
    private final RefreshTokenHasher hasher;
    private final AccountAuthService accountAuthService;

    @Transactional
    public String createOneTimeCode(SocialProviderProfile profile) {
        SocialProviderProfile normalizedProfile = normalized(profile);

        Account account = findOrCreateAccount(normalizedProfile);
        String rawCode = codeGenerator.generate();
        String hash = hasher.hash(rawCode);
        Instant expiresAt = Instant.now().plus(CODE_DURATION);

        SocialLoginCode savedCode = codeRepository.save(SocialLoginCode.builder()
                .accountId(account.getId())
                .hash(hash)
                .expiresAt(expiresAt)
                .build());

        return rawCode;
    }

    @Transactional
    public AccountLoginSessionDTO exchangeCode(
            String rawCode,
            String ipAddress,
            String userAgent,
            String deviceName
    ) {
        String hash = hashCode(rawCode);
        SocialLoginCode code = codeRepository.consumeValid(hash, Instant.now())
                .orElseThrow(() -> new InvalidTokenException("SOCIAL_CODE_INVALID"));

        Account account = accountRepository.findById(code.getAccountId())
                .orElseThrow(() -> new InvalidTokenException("SOCIAL_CODE_INVALID"));

        return accountAuthService.createAuthenticatedSession(
                account,
                ipAddress,
                userAgent,
                deviceName
        );
    }

    private Account findOrCreateAccount(SocialProviderProfile profile) {
        validate(profile);

        Optional<SocialIdentity> existingIdentity = identityRepository
                .findByProviderAndProviderUserId(profile.provider(), profile.providerUserId());

        if (existingIdentity.isPresent()) {
            SocialIdentity identity = existingIdentity.get();

            return findAccountOrFail(identity.getAccountId());
        }

        return createOrLinkAccount(profile);
    }

    private Account createOrLinkAccount(SocialProviderProfile profile) {
        Optional<Account> existingAccount = accountService.findOptionalByEmailIgnoreCase(profile.email());

        Account account;
        boolean shouldUpdateAvatarAfterLink = false;
        if (existingAccount.isPresent()) {
            account = existingAccount.get();
            shouldUpdateAvatarAfterLink = isFirstSocialLink(account);
        } else {
            account = accountService.createSocial(profile);
        }

        try {
            identityRepository.save(SocialIdentity.builder()
                    .accountId(account.getId())
                    .provider(profile.provider())
                    .providerUserId(profile.providerUserId())
                    .providerEmail(profile.email())
                    .build());
        } catch (SocialIdentityConflictException exception) {
            return findLinkedAccountAfterConflict(profile);
        }

        if (shouldUpdateAvatarAfterLink) {
            return accountService.updateAvatarWhenPresent(account, profile.avatarUrl());
        }

        return account;
    }

    private Account findLinkedAccountAfterConflict(SocialProviderProfile profile) {
        SocialIdentity existingIdentity = identityRepository
                .findByProviderAndProviderUserId(profile.provider(), profile.providerUserId())
                .orElseThrow(() -> new SocialLoginException(SocialLoginErrorCode.SOCIAL_LOGIN_FAILED));

        return findAccountOrFail(existingIdentity.getAccountId());
    }

    private Account findAccountOrFail(String accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new SocialLoginException(SocialLoginErrorCode.SOCIAL_LOGIN_FAILED));
    }

    private boolean isFirstSocialLink(Account account) {
        return !identityRepository.existsByAccountId(account.getId());
    }

    private SocialProviderProfile normalized(SocialProviderProfile profile) {
        if (profile == null) {
            throw new SocialLoginException(SocialLoginErrorCode.SOCIAL_LOGIN_FAILED);
        }

        String email = profile.email();
        if (email != null) {
            email = email.trim().toLowerCase(Locale.ROOT);
        }

        return profile.withEmail(email);
    }

    private void validate(SocialProviderProfile profile) {
        if (profile.provider() == null || isBlank(profile.providerUserId())) {
            throw new SocialLoginException(SocialLoginErrorCode.SOCIAL_PROVIDER_ERROR);
        }

        if (isBlank(profile.email())) {
            throw new SocialLoginException(SocialLoginErrorCode.SOCIAL_EMAIL_UNAVAILABLE);
        }

        if (!profile.emailVerified()) {
            throw new SocialLoginException(SocialLoginErrorCode.SOCIAL_EMAIL_NOT_VERIFIED);
        }
    }

    private String hashCode(String rawCode) {
        if (isBlank(rawCode)) {
            throw new InvalidTokenException("SOCIAL_CODE_INVALID");
        }

        return hasher.hash(rawCode);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
