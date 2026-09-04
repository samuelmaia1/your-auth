package com.samuelmaia1_github.yourauth.account.unit;

import com.samuelmaia1_github.yourauth.domain.account.Account;
import com.samuelmaia1_github.yourauth.domain.account.AccountPolicy;
import com.samuelmaia1_github.yourauth.domain.account.AccountRepository;
import com.samuelmaia1_github.yourauth.domain.account.AccountService;
import com.samuelmaia1_github.yourauth.domain.account.exceptions.AccountNotFoundException;
import com.samuelmaia1_github.yourauth.domain.subscription.AccountSubscription;
import com.samuelmaia1_github.yourauth.domain.subscription.AccountSubscriptionService;
import com.samuelmaia1_github.yourauth.domain.valueobjects.CPF;
import com.samuelmaia1_github.yourauth.infra.interfaces.IPasswordEncoder;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class AccountServiceTest {
    @Test
    void shouldCreateAccount() {
        Account account = Account.builder()
                .id("account-id")
                .email("email@email.com")
                .password("raw-password")
                .build();
        RecordingAccountRepository repository = new RecordingAccountRepository();
        RecordingAccountPolicy policy = new RecordingAccountPolicy();
        StubPasswordEncoder encoder = new StubPasswordEncoder();
        RecordingAccountSubscriptionService subscriptionService = new RecordingAccountSubscriptionService();
        AccountService service = new AccountService(repository, policy, encoder, subscriptionService);

        Account createdAccount = service.create(account);

        assertThat(policy.checkedAccount).isSameAs(account);
        assertThat(encoder.rawPassword).isEqualTo("raw-password");
        assertThat(repository.savedAccount).isSameAs(account);
        assertThat(subscriptionService.accountId).isEqualTo("account-id");
        assertThat(createdAccount).isSameAs(account);
        assertThat(createdAccount.getPassword()).isEqualTo("encoded-password");
    }

    @Test
    void shouldFindAccountByAuthenticatedId() {
        Account account = Account.builder()
                .id("account-id")
                .email("email@email.com")
                .build();
        RecordingAccountRepository repository = new RecordingAccountRepository();
        repository.accountById = Optional.of(account);
        AccountService service = new AccountService(
                repository,
                new RecordingAccountPolicy(),
                new StubPasswordEncoder(),
                new RecordingAccountSubscriptionService()
        );

        Account foundAccount = service.findByIdOrEmail("account-id", "email@email.com");

        assertThat(foundAccount).isSameAs(account);
        assertThat(repository.searchedId).isEqualTo("account-id");
        assertThat(repository.searchedEmail).isNull();
    }

    @Test
    void shouldFindAccountByAuthenticatedEmailWhenIdDoesNotMatch() {
        Account account = Account.builder()
                .id("account-id")
                .email("email@email.com")
                .build();
        RecordingAccountRepository repository = new RecordingAccountRepository();
        repository.accountByEmail = Optional.of(account);
        AccountService service = new AccountService(
                repository,
                new RecordingAccountPolicy(),
                new StubPasswordEncoder(),
                new RecordingAccountSubscriptionService()
        );

        Account foundAccount = service.findByIdOrEmail("missing-id", "email@email.com");

        assertThat(foundAccount).isSameAs(account);
        assertThat(repository.searchedId).isEqualTo("missing-id");
        assertThat(repository.searchedEmail).isEqualTo("email@email.com");
    }

    @Test
    void shouldThrowWhenAuthenticatedAccountIsNotFound() {
        RecordingAccountRepository repository = new RecordingAccountRepository();
        AccountService service = new AccountService(
                repository,
                new RecordingAccountPolicy(),
                new StubPasswordEncoder(),
                new RecordingAccountSubscriptionService()
        );

        assertThatThrownBy(() -> service.findByIdOrEmail("missing-id", "missing@email.com"))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessage("Conta não encontrada.");

        assertThat(repository.searchedId).isEqualTo("missing-id");
        assertThat(repository.searchedEmail).isEqualTo("missing@email.com");
    }

    private static class RecordingAccountPolicy extends AccountPolicy {
        private Account checkedAccount;

        private RecordingAccountPolicy() {
            super(new RecordingAccountRepository());
        }

        @Override
        public void ensureCanCreate(Account account) {
            checkedAccount = account;
        }
    }

    private static class RecordingAccountRepository implements AccountRepository {
        private Account savedAccount;
        private Optional<Account> accountById = Optional.empty();
        private Optional<Account> accountByEmail = Optional.empty();
        private String searchedId;
        private String searchedEmail;

        @Override
        public Account save(Account account) {
            savedAccount = account;
            return account;
        }

        @Override
        public Optional<Account> findById(String id) {
            searchedId = id;
            return accountById;
        }

        @Override
        public Optional<Account> findByEmail(String email) {
            searchedEmail = email;
            return accountByEmail;
        }

        @Override
        public Optional<Account> findByCPF(CPF cpf) {
            return Optional.empty();
        }

        @Override
        public Optional<Account> findByEmailIgnoreCaseOrCPF(String email, CPF cpf) {
            return Optional.empty();
        }

        @Override
        public void deleteById(String id) {
        }
    }

    private static class StubPasswordEncoder implements IPasswordEncoder {
        private String rawPassword;

        @Override
        public String encode(String raw) {
            rawPassword = raw;
            return "encoded-password";
        }

        @Override
        public Boolean matches(String raw, String hash) {
            return false;
        }
    }

    private static class RecordingAccountSubscriptionService extends AccountSubscriptionService {
        private String accountId;

        private RecordingAccountSubscriptionService() {
            super(null, null, null);
        }

        @Override
        public AccountSubscription createFreeSubscription(String accountId) {
            this.accountId = accountId;
            return null;
        }
    }
}
