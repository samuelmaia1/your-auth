package com.samuelmaia1_github.yourauth.domain.account;

import java.util.List;

public record AccountSummary(List<AccountProjectSummary> projects) {
    public AccountSummary {
        projects = projects == null ? List.of() : List.copyOf(projects);
    }

    public long totalProjects() {
        return projects.size();
    }

    public long totalUsers() {
        return projects.stream()
                .mapToLong(AccountProjectSummary::totalUsers)
                .sum();
    }

    public long totalActiveSessions() {
        return projects.stream()
                .mapToLong(AccountProjectSummary::totalActiveSessions)
                .sum();
    }
}
