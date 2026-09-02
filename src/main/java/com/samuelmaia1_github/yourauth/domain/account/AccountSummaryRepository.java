package com.samuelmaia1_github.yourauth.domain.account;

import java.util.List;

public interface AccountSummaryRepository {
    List<AccountProjectSummary> findProjectSummariesByAccountId(String accountId);
}
