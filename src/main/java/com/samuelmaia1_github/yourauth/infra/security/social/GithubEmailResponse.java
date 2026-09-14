package com.samuelmaia1_github.yourauth.infra.security.social;

public record GithubEmailResponse(
        String email,
        Boolean primary,
        Boolean verified
) {
}
