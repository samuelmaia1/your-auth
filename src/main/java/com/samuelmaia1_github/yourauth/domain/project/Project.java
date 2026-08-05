package com.samuelmaia1_github.yourauth.domain.project;

import com.samuelmaia1_github.yourauth.domain.secretkey.SecretKey;
import com.samuelmaia1_github.yourauth.domain.account.Account;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder
public class Project {
    private String title;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String accountId;
    private ProjectType type;

    private final List<SecretKey> secretKeys = new ArrayList<>();
    private final List<Account> collaborators = new ArrayList<>();
}
