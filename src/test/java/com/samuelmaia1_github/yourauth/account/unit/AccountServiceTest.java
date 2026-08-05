package com.samuelmaia1_github.yourauth.account.unit;

import com.samuelmaia1_github.yourauth.domain.account.Account;
import com.samuelmaia1_github.yourauth.domain.account.AccountPolicy;
import com.samuelmaia1_github.yourauth.domain.account.AccountRepository;
import com.samuelmaia1_github.yourauth.domain.account.AccountService;
import com.samuelmaia1_github.yourauth.domain.valueobjects.CPF;
import com.samuelmaia1_github.yourauth.infra.interfaces.IPasswordEncoder;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class AccountServiceTest {
    @Test
    void shouldCreateAccount() {
        Account account = Account.builder()
                .email("email@email.com")
                .password("raw-password")
                .build();
        RecordingAccountRepository repository = new RecordingAccountRepository();
        RecordingAccountPolicy policy = new RecordingAccountPolicy();
        StubPasswordEncoder encoder = new StubPasswordEncoder();
        AccountService service = new AccountService(repository, policy, encoder);

        Account createdAccount = service.create(account);

        assertThat(policy.checkedAccount).isSameAs(account);
        assertThat(encoder.rawPassword).isEqualTo("raw-password");
        assertThat(repository.savedAccount).isSameAs(account);
        assertThat(createdAccount).isSameAs(account);
        assertThat(createdAccount.getPassword()).isEqualTo("encoded-password");
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

        @Override
        public Account save(Account account) {
            savedAccount = account;
            return account;
        }

        @Override
        public Optional<Account> findById(String id) {
            return Optional.empty();
        }

        @Override
        public Optional<Account> findByEmail(String email) {
            return Optional.empty();
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
}
