package com.samuelmaia1_github.yourauth.domain.projectapikey;

import com.samuelmaia1_github.yourauth.domain.account.Account;

public record ProjectApiKeyDetails(
        ProjectApiKey apiKey,
        Account createdByAccount
) {
}
