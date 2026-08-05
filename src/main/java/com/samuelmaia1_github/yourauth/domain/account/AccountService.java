package com.samuelmaia1_github.yourauth.domain.account;

import com.samuelmaia1_github.yourauth.infra.interfaces.IPasswordEncoder;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository repository;
    private final AccountPolicy policy;
    private final IPasswordEncoder encoder;

    @Transactional
    public Account create(Account account) {
        policy.ensureCanCreate(account);

        account.updatePassword(encoder.encode(account.getPassword()));

        return repository.save(account);
    }
}
