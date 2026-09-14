package com.samuelmaia1_github.yourauth.infra.security.social;

import com.samuelmaia1_github.yourauth.domain.social.SocialLoginErrorCode;
import com.samuelmaia1_github.yourauth.domain.social.SocialProvider;
import com.samuelmaia1_github.yourauth.domain.social.SocialProviderProfile;
import com.samuelmaia1_github.yourauth.domain.social.exceptions.SocialLoginException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SocialOAuth2ProfileResolver {
    private static final String GOOGLE_REGISTRATION_ID = "google";
    private static final String GITHUB_REGISTRATION_ID = "github";

    private final OAuth2AuthorizedClientService authorizedClientService;
    private final GithubEmailClient githubEmailClient;

    public SocialProviderProfile resolve(OAuth2AuthenticationToken authentication) {
        String registrationId = authentication.getAuthorizedClientRegistrationId();
        Map<String, Object> attributes = authentication.getPrincipal().getAttributes();

        if (GOOGLE_REGISTRATION_ID.equals(registrationId)) {
            return googleProfile(attributes);
        }

        if (GITHUB_REGISTRATION_ID.equals(registrationId)) {
            return githubProfile(authentication, attributes);
        }

        throw new SocialLoginException(SocialLoginErrorCode.SOCIAL_PROVIDER_ERROR);
    }

    private SocialProviderProfile googleProfile(Map<String, Object> attributes) {
        String providerUserId = stringAttribute(attributes, "sub");
        String email = stringAttribute(attributes, "email");
        boolean emailVerified = booleanAttribute(attributes, "email_verified");

        if (isBlank(email)) {
            throw new SocialLoginException(SocialLoginErrorCode.SOCIAL_EMAIL_UNAVAILABLE);
        }

        if (!emailVerified) {
            throw new SocialLoginException(SocialLoginErrorCode.SOCIAL_EMAIL_NOT_VERIFIED);
        }

        return new SocialProviderProfile(
                SocialProvider.GOOGLE,
                providerUserId,
                email,
                true,
                stringAttribute(attributes, "given_name"),
                stringAttribute(attributes, "family_name"),
                stringAttribute(attributes, "picture")
        );
    }

    private SocialProviderProfile githubProfile(
            OAuth2AuthenticationToken authentication,
            Map<String, Object> attributes
    ) {
        String providerUserId = stringAttribute(attributes, "id");

        String accessToken = accessToken(authentication);
        String email = verifiedGithubEmail(accessToken);

        return new SocialProviderProfile(
                SocialProvider.GITHUB,
                providerUserId,
                email,
                true,
                stringAttribute(attributes, "name"),
                null,
                stringAttribute(attributes, "avatar_url")
        );
    }

    private String verifiedGithubEmail(String accessToken) {
        var emails = githubEmailClient.findEmails(accessToken);

        if (emails.isEmpty()) {
            throw new SocialLoginException(SocialLoginErrorCode.SOCIAL_EMAIL_UNAVAILABLE);
        }

        Optional<String> selectedEmail = emails.stream()
                .filter(githubEmail -> Boolean.TRUE.equals(githubEmail.primary()))
                .filter(githubEmail -> Boolean.TRUE.equals(githubEmail.verified()))
                .map(GithubEmailResponse::email)
                .filter(githubEmail -> !isBlank(githubEmail))
                .findFirst();

        if (selectedEmail.isEmpty()) {
            throw new SocialLoginException(SocialLoginErrorCode.SOCIAL_EMAIL_NOT_VERIFIED);
        }

        return selectedEmail.get();
    }

    private String accessToken(OAuth2AuthenticationToken authentication) {
        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(),
                authentication.getName()
        );

        if (authorizedClient == null || authorizedClient.getAccessToken() == null) {
            throw new SocialLoginException(SocialLoginErrorCode.SOCIAL_PROVIDER_ERROR);
        }

        return authorizedClient.getAccessToken().getTokenValue();
    }

    private String stringAttribute(Map<String, Object> attributes, String name) {
        Object value = attributes.get(name);
        return value == null ? null : value.toString();
    }

    private boolean booleanAttribute(Map<String, Object> attributes, String name) {
        Object value = attributes.get(name);

        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }

        return value != null && Boolean.parseBoolean(value.toString());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
