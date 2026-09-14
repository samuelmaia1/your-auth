package com.samuelmaia1_github.yourauth.infra.security.social;

import java.util.List;

public interface GithubEmailClient {
    List<GithubEmailResponse> findEmails(String accessToken);
}
