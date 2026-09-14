package com.samuelmaia1_github.yourauth.infra.security.social;

import com.samuelmaia1_github.yourauth.domain.social.SocialLoginErrorCode;
import com.samuelmaia1_github.yourauth.domain.social.exceptions.SocialLoginException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Component
public class GithubEmailRestClient implements GithubEmailClient {
    private static final ParameterizedTypeReference<List<GithubEmailResponse>> EMAIL_LIST =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient restClient;

    public GithubEmailRestClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder
                .baseUrl("https://api.github.com")
                .build();
    }

    @Override
    public List<GithubEmailResponse> findEmails(String accessToken) {
        try {
            List<GithubEmailResponse> emails = restClient
                    .get()
                    .uri("/user/emails")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .header(HttpHeaders.ACCEPT, "application/vnd.github+json")
                    .retrieve()
                    .body(EMAIL_LIST);

            return emails == null ? List.of() : emails;
        } catch (RestClientException exception) {
            throw new SocialLoginException(SocialLoginErrorCode.SOCIAL_PROVIDER_ERROR, exception);
        }
    }
}
