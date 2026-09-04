package com.samuelmaia1_github.yourauth.domain.account;

import com.samuelmaia1_github.yourauth.domain.account.exceptions.AccountNotFoundException;
import com.samuelmaia1_github.yourauth.domain.subscription.AccountSubscriptionService;
import com.samuelmaia1_github.yourauth.infra.interfaces.IPasswordEncoder;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

        Account createdAccount = repository.save(account);
        subscriptionService.createFreeSubscription(createdAccount.getId());

        return createdAccount;
    }

    public Account findByIdOrEmail(String id, String email) {
        return findById(id)
                .or(() -> findByEmail(email))
                .orElseThrow(AccountNotFoundException::new);
    }

    private Optional<Account> findById(String id) {
        if (isBlank(id)) {
            return Optional.empty();
        }

        return repository.findById(id);
    }

    private Optional<Account> findByEmail(String email) {
        if (isBlank(email)) {
            return Optional.empty();
        }

        return repository.findByEmail(email);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
