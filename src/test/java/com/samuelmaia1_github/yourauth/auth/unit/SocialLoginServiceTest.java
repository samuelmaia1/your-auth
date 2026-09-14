package com.samuelmaia1_github.yourauth.auth.unit;

import com.samuelmaia1_github.yourauth.domain.account.Account;
import com.samuelmaia1_github.yourauth.domain.account.AccountRepository;
import com.samuelmaia1_github.yourauth.domain.account.AccountService;
import com.samuelmaia1_github.yourauth.domain.accountsession.AccountSessionRepository;
import com.samuelmaia1_github.yourauth.domain.auth.AccountAuthService;
import com.samuelmaia1_github.yourauth.domain.auth.TokenService;
import com.samuelmaia1_github.yourauth.domain.auth.exceptions.InvalidTokenException;
import com.samuelmaia1_github.yourauth.domain.refreshtoken.AccountRefreshTokenService;
import com.samuelmaia1_github.yourauth.domain.refreshtoken.RefreshTokenHasher;
import com.samuelmaia1_github.yourauth.domain.social.SocialIdentity;
import com.samuelmaia1_github.yourauth.domain.social.SocialIdentityRepository;
import com.samuelmaia1_github.yourauth.domain.social.SocialLoginCode;
import com.samuelmaia1_github.yourauth.domain.social.SocialLoginCodeGenerator;
import com.samuelmaia1_github.yourauth.domain.social.SocialLoginCodeRepository;
import com.samuelmaia1_github.yourauth.domain.social.SocialLoginErrorCode;
import com.samuelmaia1_github.yourauth.domain.social.SocialLoginService;
import com.samuelmaia1_github.yourauth.domain.social.SocialProvider;
import com.samuelmaia1_github.yourauth.domain.social.SocialProviderProfile;
import com.samuelmaia1_github.yourauth.domain.social.exceptions.SocialIdentityConflictException;
import com.samuelmaia1_github.yourauth.domain.social.exceptions.SocialLoginException;
import com.samuelmaia1_github.yourauth.domain.valueobjects.CPF;
import com.samuelmaia1_github.yourauth.infra.interfaces.IPasswordEncoder;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.AccountLoginSessionDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.user.TokenDTO;
import com.samuelmaia1_github.yourauth.presentation.mapper.AccountPresentationMapper;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SocialLoginServiceTest {
    private static final String ACCOUNT_ID = "account-id";

    @Test
    void shouldCreateAccountByGoogleAndPersistIdentityAndOneTimeCode() {
        Fixture fixture = fixture("one-time-code");

        String code = fixture.service.createOneTimeCode(googleProfile());

        assertThat(code).isEqualTo("one-time-code");
        assertThat(fixture.accountService.createdSocialAccount.getEmail()).isEqualTo("google@example.com");
        assertThat(fixture.accountService.createdSocialAccount.getPassword()).isNull();
        assertThat(fixture.accountService.createdSocialAccount.getCPF()).isNull();
        assertThat(fixture.accountService.createdSocialAccount.getPhone()).isNull();
        assertThat(fixture.accountService.createdSocialAccount.getAddress()).isNull();
        assertThat(fixture.accountService.createdSocialAccount.getAvatarUrl()).isEqualTo("https://google/avatar.png");
        assertThat(fixture.identityRepository.savedIdentities).hasSize(1);
        assertThat(fixture.identityRepository.savedIdentities.getFirst().getProvider()).isEqualTo(SocialProvider.GOOGLE);
        assertThat(fixture.identityRepository.savedIdentities.getFirst().getProviderUserId()).isEqualTo("google-sub");
        assertThat(fixture.codeRepository.savedCodes).hasSize(1);
        assertThat(fixture.codeRepository.savedCodes.getFirst().getHash()).isNotEqualTo("one-time-code");
        assertThat(fixture.codeRepository.savedCodes.getFirst().getExpiresAt())
                .isBetween(Instant.now().plusSeconds(30), Instant.now().plusSeconds(60));
    }

    @Test
    void shouldCreateAccountByGithub() {
        Fixture fixture = fixture("github-code");

        fixture.service.createOneTimeCode(githubProfile());

        assertThat(fixture.accountService.createdSocialAccount.getEmail()).isEqualTo("github@example.com");
        assertThat(fixture.accountService.createdSocialAccount.getName()).isEqualTo("Git Hub");
        assertThat(fixture.accountService.createdSocialAccount.getLastName()).isNull();
        assertThat(fixture.accountService.createdSocialAccount.getAvatarUrl()).isEqualTo("https://github/avatar.png");
        assertThat(fixture.identityRepository.savedIdentities.getFirst().getProvider()).isEqualTo(SocialProvider.GITHUB);
        assertThat(fixture.identityRepository.savedIdentities.getFirst().getProviderUserId()).isEqualTo("123456");
    }

    @Test
    void shouldLoginWithExistingSocialIdentityWithoutUpdatingAvatar() {
        Fixture fixture = fixture("existing-code");
        Account account = account("linked-account", "linked@example.com", "https://current/avatar.png");
        fixture.accountRepository.accounts.add(account);
        fixture.identityRepository.identities.add(SocialIdentity.builder()
                .accountId("linked-account")
                .provider(SocialProvider.GOOGLE)
                .providerUserId("google-sub")
                .providerEmail("linked@example.com")
                .build());

        fixture.service.createOneTimeCode(googleProfile());

        assertThat(fixture.accountService.createdSocialAccount).isNull();
        assertThat(fixture.identityRepository.savedIdentities).isEmpty();
        assertThat(account.getAvatarUrl()).isEqualTo("https://current/avatar.png");
    }

    @Test
    void shouldLinkFirstSocialIdentityToExistingAccountWithVerifiedEmailAndUpdateAvatar() {
        Fixture fixture = fixture("link-code");
        Account existingAccount = account(ACCOUNT_ID, "google@example.com", null);
        fixture.accountRepository.accounts.add(existingAccount);

        fixture.service.createOneTimeCode(googleProfile());

        assertThat(fixture.accountService.createdSocialAccount).isNull();
        assertThat(fixture.identityRepository.savedIdentities).hasSize(1);
        assertThat(fixture.identityRepository.savedIdentities.getFirst().getAccountId()).isEqualTo(ACCOUNT_ID);
        assertThat(existingAccount.getAvatarUrl()).isEqualTo("https://google/avatar.png");
    }

    @Test
    void shouldLinkAnotherProviderToExistingSocialAccountWithoutUpdatingAvatar() {
        Fixture fixture = fixture("link-google-code");
        Account existingAccount = account(ACCOUNT_ID, "social@example.com", "https://github/avatar.png");
        fixture.accountRepository.accounts.add(existingAccount);
        fixture.identityRepository.identities.add(SocialIdentity.builder()
                .accountId(ACCOUNT_ID)
                .provider(SocialProvider.GITHUB)
                .providerUserId("github-sub")
                .providerEmail("social@example.com")
                .build());

        fixture.service.createOneTimeCode(new SocialProviderProfile(
                SocialProvider.GOOGLE,
                "google-sub",
                "social@example.com",
                true,
                "Google",
                "Account",
                "https://google/avatar.png"
        ));

        assertThat(fixture.identityRepository.savedIdentities).hasSize(1);
        assertThat(fixture.identityRepository.savedIdentities.getFirst().getProvider()).isEqualTo(SocialProvider.GOOGLE);
        assertThat(existingAccount.getAvatarUrl()).isEqualTo("https://github/avatar.png");
    }

    @Test
    void shouldRejectUnverifiedProviderEmail() {
        Fixture fixture = fixture("unused-code");
        SocialProviderProfile profile = new SocialProviderProfile(
                SocialProvider.GOOGLE,
                "google-sub",
                "google@example.com",
                false,
                "Google",
                "Account",
                null
        );

        assertThatThrownBy(() -> fixture.service.createOneTimeCode(profile))
                .isInstanceOf(SocialLoginException.class)
                .extracting("code")
                .isEqualTo(SocialLoginErrorCode.SOCIAL_EMAIL_NOT_VERIFIED);
    }

    @Test
    void shouldNotOverwriteExistingAvatarWithNull() {
        Fixture fixture = fixture("avatar-code");
        Account account = account(ACCOUNT_ID, "google@example.com", "https://current/avatar.png");
        fixture.accountRepository.accounts.add(account);
        fixture.identityRepository.identities.add(SocialIdentity.builder()
                .accountId(ACCOUNT_ID)
                .provider(SocialProvider.GOOGLE)
                .providerUserId("google-sub")
                .providerEmail("google@example.com")
                .build());

        fixture.service.createOneTimeCode(new SocialProviderProfile(
                SocialProvider.GOOGLE,
                "google-sub",
                "google@example.com",
                true,
                "Google",
                "Account",
                null
        ));

        assertThat(account.getAvatarUrl()).isEqualTo("https://current/avatar.png");
    }

    @Test
    void shouldRecoverExistingIdentityAfterUniqueConstraintConflict() {
        Fixture fixture = fixture("retry-code");
        Account account = account(ACCOUNT_ID, "google@example.com", null);
        fixture.accountRepository.accounts.add(account);
        fixture.identityRepository.conflictOnNextSave = true;
        fixture.identityRepository.identityAvailableAfterConflict = SocialIdentity.builder()
                .accountId(ACCOUNT_ID)
                .provider(SocialProvider.GOOGLE)
                .providerUserId("google-sub")
                .providerEmail("google@example.com")
                .build();

        fixture.service.createOneTimeCode(googleProfile());

        assertThat(fixture.codeRepository.savedCodes).hasSize(1);
        assertThat(fixture.accountService.createdSocialAccount).isNull();
    }

    @Test
    void shouldExchangeValidTemporaryCodeCreatingYourAuthTokens() {
        Fixture fixture = fixture("valid-code");
        Account account = account(ACCOUNT_ID, "social@example.com", null);
        fixture.accountRepository.accounts.add(account);
        fixture.codeRepository.codes.add(SocialLoginCode.builder()
                .accountId(ACCOUNT_ID)
                .hash(fixture.hasher.hash("valid-code"))
                .expiresAt(Instant.now().plusSeconds(45))
                .build());

        AccountLoginSessionDTO loginSession = fixture.service.exchangeCode(
                "valid-code",
                "203.0.113.10",
                "user-agent",
                "device"
        );

        assertThat(loginSession.account().id()).isEqualTo(ACCOUNT_ID);
        assertThat(loginSession.accessToken().raw()).isEqualTo("your-auth-access-token");
        assertThat(loginSession.refreshToken().raw()).isEqualTo("your-auth-refresh-token");
        assertThat(fixture.accountAuthService.account).isSameAs(account);
        assertThat(fixture.accountAuthService.ipAddress).isEqualTo("203.0.113.10");
        assertThat(fixture.codeRepository.codes.getFirst().isConsumed()).isTrue();
    }

    @Test
    void shouldRejectExpiredTemporaryCode() {
        Fixture fixture = fixture("expired-code");
        fixture.codeRepository.codes.add(SocialLoginCode.builder()
                .accountId(ACCOUNT_ID)
                .hash(fixture.hasher.hash("expired-code"))
                .expiresAt(Instant.now().minusSeconds(1))
                .build());

        assertThatThrownBy(() -> fixture.service.exchangeCode("expired-code", null, null, null))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("SOCIAL_CODE_INVALID");
    }

    @Test
    void shouldRejectReusedTemporaryCode() {
        Fixture fixture = fixture("reused-code");
        fixture.accountRepository.accounts.add(account(ACCOUNT_ID, "social@example.com", null));
        fixture.codeRepository.codes.add(SocialLoginCode.builder()
                .accountId(ACCOUNT_ID)
                .hash(fixture.hasher.hash("reused-code"))
                .expiresAt(Instant.now().plusSeconds(45))
                .build());

        fixture.service.exchangeCode("reused-code", null, null, null);

        assertThatThrownBy(() -> fixture.service.exchangeCode("reused-code", null, null, null))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("SOCIAL_CODE_INVALID");
    }

    private Fixture fixture(String generatedCode) {
        RefreshTokenHasher hasher = new RefreshTokenHasher("test-secret");
        RecordingAccountRepository accountRepository = new RecordingAccountRepository();
        RecordingAccountService accountService = new RecordingAccountService(accountRepository);
        InMemorySocialIdentityRepository identityRepository = new InMemorySocialIdentityRepository();
        InMemorySocialLoginCodeRepository codeRepository = new InMemorySocialLoginCodeRepository();
        RecordingAccountAuthService accountAuthService = new RecordingAccountAuthService();

        return new Fixture(
                hasher,
                accountRepository,
                accountService,
                identityRepository,
                codeRepository,
                accountAuthService,
                new SocialLoginService(
                        accountService,
                        accountRepository,
                        identityRepository,
                        codeRepository,
                        new FixedCodeGenerator(generatedCode),
                        hasher,
                        accountAuthService
                )
        );
    }

    private SocialProviderProfile googleProfile() {
        return new SocialProviderProfile(
                SocialProvider.GOOGLE,
                "google-sub",
                "Google@Example.com",
                true,
                "Google",
                "Account",
                "https://google/avatar.png"
        );
    }

    private SocialProviderProfile githubProfile() {
        return new SocialProviderProfile(
                SocialProvider.GITHUB,
                "123456",
                "Github@Example.com",
                true,
                "Git Hub",
                null,
                "https://github/avatar.png"
        );
    }

    private Account account(String id, String email, String avatarUrl) {
        return Account.builder()
                .id(id)
                .name("Existing")
                .lastName("Account")
                .email(email)
                .avatarUrl(avatarUrl)
                .CPF(new CPF("12345678909"))
                .password("hashed-password")
                .build();
    }

    private record Fixture(
            RefreshTokenHasher hasher,
            RecordingAccountRepository accountRepository,
            RecordingAccountService accountService,
            InMemorySocialIdentityRepository identityRepository,
            InMemorySocialLoginCodeRepository codeRepository,
            RecordingAccountAuthService accountAuthService,
            SocialLoginService service
    ) {
    }

    private static class FixedCodeGenerator extends SocialLoginCodeGenerator {
        private final String code;

        private FixedCodeGenerator(String code) {
            this.code = code;
        }

        @Override
        public String generate() {
            return code;
        }
    }

    private static class RecordingAccountService extends AccountService {
        private final RecordingAccountRepository repository;
        private Account createdSocialAccount;

        private RecordingAccountService(RecordingAccountRepository repository) {
            super(repository, null, null, null);
            this.repository = repository;
        }

        @Override
        public Account createSocial(SocialProviderProfile profile) {
            createdSocialAccount = Account.builder()
                    .id(ACCOUNT_ID)
                    .name(profile.name())
                    .lastName(profile.lastName())
                    .email(profile.email())
                    .avatarUrl(profile.avatarUrl())
                    .build();
            repository.accounts.add(createdSocialAccount);
            return createdSocialAccount;
        }

        @Override
        public Optional<Account> findOptionalByEmailIgnoreCase(String email) {
            return repository.findByEmailIgnoreCase(email);
        }

        @Override
        public Account updateAvatarWhenPresent(Account account, String avatarUrl) {
            account.updateAvatarUrl(avatarUrl);
            return account;
        }
    }

    private static class RecordingAccountAuthService extends AccountAuthService {
        private Account account;
        private String ipAddress;

        private RecordingAccountAuthService() {
            super(null, null, null, null, null);
        }

        @Override
        public AccountLoginSessionDTO createAuthenticatedSession(
                Account account,
                String ipAddress,
                String userAgent,
                String deviceName
        ) {
            this.account = account;
            this.ipAddress = ipAddress;

            return new AccountLoginSessionDTO(
                    AccountPresentationMapper.toResponseDTO(account),
                    new TokenDTO("your-auth-access-token", Duration.ofMinutes(15)),
                    new TokenDTO("your-auth-refresh-token", Duration.ofDays(7))
            );
        }
    }

    private static class RecordingAccountRepository implements AccountRepository {
        private final List<Account> accounts = new ArrayList<>();

        @Override
        public Account save(Account account) {
            accounts.removeIf(current -> current.getId().equals(account.getId()));
            accounts.add(account);
            return account;
        }

        @Override
        public Optional<Account> findById(String id) {
            return accounts.stream()
                    .filter(account -> id.equals(account.getId()))
                    .findFirst();
        }

        @Override
        public Optional<Account> findByEmail(String email) {
            return accounts.stream()
                    .filter(account -> email.equals(account.getEmail()))
                    .findFirst();
        }

        @Override
        public Optional<Account> findByEmailIgnoreCase(String email) {
            return accounts.stream()
                    .filter(account -> account.getEmail().equalsIgnoreCase(email))
                    .findFirst();
        }

        @Override
        public Optional<Account> findByCPF(CPF cpf) {
            return accounts.stream()
                    .filter(account -> cpf.equals(account.getCPF()))
                    .findFirst();
        }

        @Override
        public Optional<Account> findByEmailIgnoreCaseOrCPF(String email, CPF cpf) {
            return accounts.stream()
                    .filter(account -> account.getEmail().equalsIgnoreCase(email) || cpf.equals(account.getCPF()))
                    .findFirst();
        }

        @Override
        public void deleteById(String id) {
        }
    }

    private static class InMemorySocialIdentityRepository implements SocialIdentityRepository {
        private final List<SocialIdentity> identities = new ArrayList<>();
        private final List<SocialIdentity> savedIdentities = new ArrayList<>();
        private boolean conflictOnNextSave;
        private SocialIdentity identityAvailableAfterConflict;

        @Override
        public SocialIdentity save(SocialIdentity identity) {
            if (conflictOnNextSave) {
                conflictOnNextSave = false;
                identities.add(identityAvailableAfterConflict);
                throw new SocialIdentityConflictException();
            }

            if (findByProviderAndProviderUserId(identity.getProvider(), identity.getProviderUserId()).isPresent()
                    || findByAccountIdAndProvider(identity.getAccountId(), identity.getProvider()).isPresent()) {
                throw new SocialIdentityConflictException();
            }

            identities.add(identity);
            savedIdentities.add(identity);
            return identity;
        }

        @Override
        public Optional<SocialIdentity> findByProviderAndProviderUserId(
                SocialProvider provider,
                String providerUserId
        ) {
            return identities.stream()
                    .filter(identity -> provider == identity.getProvider())
                    .filter(identity -> providerUserId.equals(identity.getProviderUserId()))
                    .findFirst();
        }

        @Override
        public Optional<SocialIdentity> findByAccountIdAndProvider(String accountId, SocialProvider provider) {
            return identities.stream()
                    .filter(identity -> accountId.equals(identity.getAccountId()))
                    .filter(identity -> provider == identity.getProvider())
                    .findFirst();
        }

        @Override
        public boolean existsByAccountId(String accountId) {
            return identities.stream()
                    .anyMatch(identity -> accountId.equals(identity.getAccountId()));
        }
    }

    private static class InMemorySocialLoginCodeRepository implements SocialLoginCodeRepository {
        private final List<SocialLoginCode> codes = new ArrayList<>();
        private final List<SocialLoginCode> savedCodes = new ArrayList<>();

        @Override
        public SocialLoginCode save(SocialLoginCode code) {
            codes.add(code);
            savedCodes.add(code);
            return code;
        }

        @Override
        public Optional<SocialLoginCode> findByHash(String hash) {
            return codes.stream()
                    .filter(code -> hash.equals(code.getHash()))
                    .findFirst();
        }

        @Override
        public Optional<SocialLoginCode> consumeValid(String hash, Instant consumedAt) {
            Optional<SocialLoginCode> currentCode = findByHash(hash)
                    .filter(code -> !code.isConsumed())
                    .filter(code -> consumedAt.isBefore(code.getExpiresAt()));

            if (currentCode.isEmpty()) {
                return Optional.empty();
            }

            SocialLoginCode code = currentCode.get();
            SocialLoginCode consumedCode = SocialLoginCode.builder()
                        .id(code.getId())
                        .accountId(code.getAccountId())
                        .hash(code.getHash())
                        .expiresAt(code.getExpiresAt())
                        .createdAt(code.getCreatedAt())
                        .consumedAt(consumedAt)
                        .build();

            codes.remove(code);
            codes.add(consumedCode);

            return Optional.of(consumedCode);
        }
    }
}
