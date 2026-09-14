package com.samuelmaia1_github.yourauth.domain.account;

import com.samuelmaia1_github.yourauth.domain.account.exceptions.AccountNotFoundException;
import com.samuelmaia1_github.yourauth.domain.social.SocialProviderProfile;
import com.samuelmaia1_github.yourauth.domain.subscription.AccountSubscriptionService;
import com.samuelmaia1_github.yourauth.infra.interfaces.IPasswordEncoder;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository repository;
    private final AccountPolicy policy;
    private final IPasswordEncoder encoder;
    private final AccountSubscriptionService subscriptionService;

    @Transactional
    public Account create(Account account) {
        policy.ensureCanCreate(account);

        account.updatePassword(encoder.encode(account.getPassword()));

        return saveWithFreeSubscription(account);
    }

    @Transactional
    public Account createSocial(SocialProviderProfile profile) {
        Account account = Account.builder()
                .name(profile.name())
                .lastName(profile.lastName())
                .email(profile.email())
                .avatarUrl(profile.avatarUrl())
                .build();

        return saveWithFreeSubscription(account);
    }

    @Transactional
    public Account updateAvatarWhenPresent(Account account, String avatarUrl) {
        String currentAvatarUrl = account.getAvatarUrl();
        account.updateAvatarUrl(avatarUrl);

        if (Objects.equals(currentAvatarUrl, account.getAvatarUrl())) {
            return account;
        }

        return repository.save(account);
    }

    public Account findByIdOrEmail(String id, String email) {
        return findOptionalById(id)
                .or(() -> findOptionalByEmail(email))
                .orElseThrow(AccountNotFoundException::new);
    }

    public Account findByEmail(String email) {
        return findOptionalByEmail(email)
                .orElseThrow(AccountNotFoundException::new);
    }

    public Optional<Account> findOptionalByEmailIgnoreCase(String email) {
        if (isBlank(email)) {
            return Optional.empty();
        }

        return repository.findByEmailIgnoreCase(email);
    }

    private Account saveWithFreeSubscription(Account account) {
        Account createdAccount = repository.save(account);
        subscriptionService.createFreeSubscription(createdAccount.getId());

        return createdAccount;
    }

    private Optional<Account> findOptionalById(String id) {
        if (isBlank(id)) {
            return Optional.empty();
        }

        return repository.findById(id);
    }

    private Optional<Account> findOptionalByEmail(String email) {
        if (isBlank(email)) {
            return Optional.empty();
        }

        return repository.findByEmail(email);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
