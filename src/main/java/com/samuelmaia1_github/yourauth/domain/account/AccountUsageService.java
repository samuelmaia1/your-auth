package com.samuelmaia1_github.yourauth.domain.account;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountUsageService {
    private final AccountUsageRepository repository;

    public AccountUsage findByOwnerAccountId(String accountId) {
        return repository.findByOwnerAccountId(accountId);
    }
}
