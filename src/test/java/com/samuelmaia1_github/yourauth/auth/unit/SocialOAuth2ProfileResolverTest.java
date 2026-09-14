package com.samuelmaia1_github.yourauth.auth.unit;

import com.samuelmaia1_github.yourauth.domain.social.SocialLoginErrorCode;
import com.samuelmaia1_github.yourauth.domain.social.SocialProvider;
import com.samuelmaia1_github.yourauth.domain.social.SocialProviderProfile;
import com.samuelmaia1_github.yourauth.domain.social.exceptions.SocialLoginException;
import com.samuelmaia1_github.yourauth.infra.security.social.GithubEmailClient;
import com.samuelmaia1_github.yourauth.infra.security.social.GithubEmailResponse;
import com.samuelmaia1_github.yourauth.infra.security.social.SocialOAuth2ProfileResolver;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SocialOAuth2ProfileResolverTest {
    @Test
    void shouldResolveVerifiedGoogleProfile() {
        SocialOAuth2ProfileResolver resolver = new SocialOAuth2ProfileResolver(
                new StubAuthorizedClientService(),
                accessToken -> List.of()
        );

        SocialProviderProfile profile = resolver.resolve(authentication(
                "google",
                "sub",
                Map.of(
                        "sub", "google-sub",
                        "email", "google@example.com",
                        "email_verified", true,
                        "given_name", "Google",
                        "family_name", "Account",
                        "picture", "https://google/avatar.png"
                )
        ));

        assertThat(profile.provider()).isEqualTo(SocialProvider.GOOGLE);
        assertThat(profile.providerUserId()).isEqualTo("google-sub");
        assertThat(profile.email()).isEqualTo("google@example.com");
        assertThat(profile.emailVerified()).isTrue();
        assertThat(profile.avatarUrl()).isEqualTo("https://google/avatar.png");
    }

    @Test
    void shouldRejectGoogleProfileWithUnverifiedEmail() {
        SocialOAuth2ProfileResolver resolver = new SocialOAuth2ProfileResolver(
                new StubAuthorizedClientService(),
                accessToken -> List.of()
        );

        assertThatThrownBy(() -> resolver.resolve(authentication(
                "google",
                "sub",
                Map.of(
                        "sub", "google-sub",
                        "email", "google@example.com",
                        "email_verified", false
                )
        )))
                .isInstanceOf(SocialLoginException.class)
                .extracting("code")
                .isEqualTo(SocialLoginErrorCode.SOCIAL_EMAIL_NOT_VERIFIED);
    }

    @Test
    void shouldResolveGithubProfileUsingPrimaryVerifiedEmailEndpoint() {
        RecordingGithubEmailClient githubEmailClient = new RecordingGithubEmailClient(List.of(
                new GithubEmailResponse("secondary@example.com", false, true),
                new GithubEmailResponse("github@example.com", true, true)
        ));
        SocialOAuth2ProfileResolver resolver = new SocialOAuth2ProfileResolver(
                new StubAuthorizedClientService(),
                githubEmailClient
        );

        SocialProviderProfile profile = resolver.resolve(authentication(
                "github",
                "id",
                Map.of(
                        "id", 123456,
                        "email", "",
                        "name", "Git Hub",
                        "avatar_url", "https://github/avatar.png"
                )
        ));

        assertThat(githubEmailClient.accessToken).isEqualTo("github-access-token");
        assertThat(profile.provider()).isEqualTo(SocialProvider.GITHUB);
        assertThat(profile.providerUserId()).isEqualTo("123456");
        assertThat(profile.email()).isEqualTo("github@example.com");
        assertThat(profile.emailVerified()).isTrue();
        assertThat(profile.avatarUrl()).isEqualTo("https://github/avatar.png");
    }

    @Test
    void shouldRejectGithubProfileWithoutVerifiedEmail() {
        SocialOAuth2ProfileResolver resolver = new SocialOAuth2ProfileResolver(
                new StubAuthorizedClientService(),
                accessToken -> List.of(
                        new GithubEmailResponse("github@example.com", true, false),
                        new GithubEmailResponse("other@example.com", false, true)
                )
        );

        assertThatThrownBy(() -> resolver.resolve(authentication(
                "github",
                "id",
                Map.of(
                        "id", 123456,
                        "name", "Git Hub",
                        "avatar_url", "https://github/avatar.png"
                )
        )))
                .isInstanceOf(SocialLoginException.class)
                .extracting("code")
                .isEqualTo(SocialLoginErrorCode.SOCIAL_EMAIL_NOT_VERIFIED);
    }

    private OAuth2AuthenticationToken authentication(
            String registrationId,
            String nameAttributeKey,
            Map<String, Object> attributes
    ) {
        DefaultOAuth2User principal = new DefaultOAuth2User(
                List.of(),
                attributes,
                nameAttributeKey
        );

        return new OAuth2AuthenticationToken(principal, List.of(), registrationId);
    }

    private static class RecordingGithubEmailClient implements GithubEmailClient {
        private final List<GithubEmailResponse> emails;
        private String accessToken;

        private RecordingGithubEmailClient(List<GithubEmailResponse> emails) {
            this.emails = emails;
        }

        @Override
        public List<GithubEmailResponse> findEmails(String accessToken) {
            this.accessToken = accessToken;
            return emails;
        }
    }

    private static class StubAuthorizedClientService implements OAuth2AuthorizedClientService {
        @Override
        @SuppressWarnings("unchecked")
        public <T extends OAuth2AuthorizedClient> T loadAuthorizedClient(
                String clientRegistrationId,
                String principalName
        ) {
            ClientRegistration registration = ClientRegistration
                    .withRegistrationId(clientRegistrationId)
                    .clientId("client-id")
                    .clientSecret("client-secret")
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .redirectUri("http://localhost/login/oauth2/code/" + clientRegistrationId)
                    .authorizationUri("https://provider.example/authorize")
                    .tokenUri("https://provider.example/token")
                    .userInfoUri("https://provider.example/user")
                    .userNameAttributeName("id")
                    .clientName(clientRegistrationId)
                    .build();

            OAuth2AccessToken accessToken = new OAuth2AccessToken(
                    OAuth2AccessToken.TokenType.BEARER,
                    "github-access-token",
                    Instant.now(),
                    Instant.now().plusSeconds(60)
            );

            return (T) new OAuth2AuthorizedClient(registration, principalName, accessToken);
        }

        @Override
        public void saveAuthorizedClient(OAuth2AuthorizedClient authorizedClient, Authentication principal) {
        }

        @Override
        public void removeAuthorizedClient(String clientRegistrationId, String principalName) {
        }
    }
}
