package com.samuelmaia1_github.yourauth.domain.account;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountSummaryService {
    private final AccountSummaryRepository repository;

    public AccountSummary findByAccountId(String accountId) {
        return new AccountSummary(repository.findProjectSummariesByAccountId(accountId));
    }
}
