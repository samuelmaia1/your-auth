package com.samuelmaia1_github.yourauth.auth.unit;

import com.samuelmaia1_github.yourauth.domain.social.SocialLoginErrorCode;
import com.samuelmaia1_github.yourauth.domain.social.SocialLoginService;
import com.samuelmaia1_github.yourauth.domain.social.SocialProvider;
import com.samuelmaia1_github.yourauth.domain.social.SocialProviderProfile;
import com.samuelmaia1_github.yourauth.domain.social.exceptions.SocialLoginException;
import com.samuelmaia1_github.yourauth.infra.security.social.SocialLoginRedirectUrlFactory;
import com.samuelmaia1_github.yourauth.infra.security.social.SocialOAuth2FailureHandler;
import com.samuelmaia1_github.yourauth.infra.security.social.SocialOAuth2ProfileResolver;
import com.samuelmaia1_github.yourauth.infra.security.social.SocialOAuth2SuccessHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SocialOAuth2HandlerTest {
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldRedirectSuccessWithOnlyOneTimeCode() throws Exception {
        SocialOAuth2SuccessHandler handler = new SocialOAuth2SuccessHandler(
                new StubProfileResolver(null),
                new StubSocialLoginService("one-time-code"),
                new SocialLoginRedirectUrlFactory("http://localhost:3000/auth/callback")
        );
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(
                new MockHttpServletRequest(),
                response,
                authentication()
        );

        assertThat(response.getRedirectedUrl())
                .isEqualTo("http://localhost:3000/auth/callback?code=one-time-code");
    }

    @Test
    void shouldCreateOneTimeCodeBeforeClearingOAuthContextAndInvalidatingTemporarySession() throws Exception {
        List<String> events = new ArrayList<>();
        OAuth2AuthenticationToken authentication = authentication();
        RecordingProfileResolver profileResolver = new RecordingProfileResolver(events);
        RecordingSocialLoginService socialLoginService = new RecordingSocialLoginService("one-time-code", events);
        SocialOAuth2SuccessHandler handler = new SocialOAuth2SuccessHandler(
                profileResolver,
                socialLoginService,
                new SocialLoginRedirectUrlFactory("http://localhost:3000/auth/callback")
        );
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        MockHttpServletResponse response = new MockHttpServletResponse();
        SecurityContextHolder.getContext().setAuthentication(authentication);
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext()
        );
        request.setSession(session);

        handler.onAuthenticationSuccess(request, response, authentication);

        assertThat(events).containsExactly("resolve-profile", "create-one-time-code");
        assertThat(profileResolver.authenticationDuringResolve).isSameAs(authentication);
        assertThat(socialLoginService.authenticationDuringCodeCreation).isSameAs(authentication);
        assertThat(socialLoginService.profile).isEqualTo(profileResolver.profile);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(session.isInvalid()).isTrue();
        assertThat(response.getRedirectedUrl())
                .isEqualTo("http://localhost:3000/auth/callback?code=one-time-code");
    }

    @Test
    void shouldRedirectProviderErrorWithStableCode() throws Exception {
        SocialOAuth2SuccessHandler handler = new SocialOAuth2SuccessHandler(
                new StubProfileResolver(new SocialLoginException(SocialLoginErrorCode.SOCIAL_EMAIL_UNAVAILABLE)),
                new StubSocialLoginService("unused-code"),
                new SocialLoginRedirectUrlFactory("http://localhost:3000/auth/callback")
        );
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(
                new MockHttpServletRequest(),
                response,
                authentication()
        );

        assertThat(response.getRedirectedUrl())
                .isEqualTo("http://localhost:3000/auth/callback?error=SOCIAL_EMAIL_UNAVAILABLE");
    }

    @Test
    void shouldRedirectDeniedOAuthFailureWithStableCode() throws Exception {
        SocialOAuth2FailureHandler handler = new SocialOAuth2FailureHandler(
                new SocialLoginRedirectUrlFactory("http://localhost:3000/auth/callback")
        );
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationFailure(
                new MockHttpServletRequest(),
                response,
                new OAuth2AuthenticationException(new OAuth2Error("access_denied"))
        );

        assertThat(response.getRedirectedUrl())
                .isEqualTo("http://localhost:3000/auth/callback?error=SOCIAL_LOGIN_DENIED");
    }

    private OAuth2AuthenticationToken authentication() {
        DefaultOAuth2User principal = new DefaultOAuth2User(
                List.of(),
                Map.of("sub", "google-sub"),
                "sub"
        );

        return new OAuth2AuthenticationToken(principal, List.of(), "google");
    }

    private static class StubProfileResolver extends SocialOAuth2ProfileResolver {
        private final SocialLoginException exception;

        private StubProfileResolver(SocialLoginException exception) {
            super(null, null);
            this.exception = exception;
        }

        @Override
        public SocialProviderProfile resolve(OAuth2AuthenticationToken authentication) {
            if (exception != null) {
                throw exception;
            }

            return new SocialProviderProfile(
                    SocialProvider.GOOGLE,
                    "google-sub",
                    "google@example.com",
                    true,
                    "Google",
                    "Account",
                    null
            );
        }
    }

    private static class StubSocialLoginService extends SocialLoginService {
        private final String code;

        private StubSocialLoginService(String code) {
            super(null, null, null, null, null, null, null);
            this.code = code;
        }

        @Override
        public String createOneTimeCode(SocialProviderProfile profile) {
            return code;
        }
    }

    private static class RecordingProfileResolver extends SocialOAuth2ProfileResolver {
        private final List<String> events;
        private final SocialProviderProfile profile = new SocialProviderProfile(
                SocialProvider.GOOGLE,
                "google-sub",
                "google@example.com",
                true,
                "Google",
                "Account",
                null
        );
        private Authentication authenticationDuringResolve;

        private RecordingProfileResolver(List<String> events) {
            super(null, null);
            this.events = events;
        }

        @Override
        public SocialProviderProfile resolve(OAuth2AuthenticationToken authentication) {
            events.add("resolve-profile");
            authenticationDuringResolve = SecurityContextHolder.getContext().getAuthentication();

            return profile;
        }
    }

    private static class RecordingSocialLoginService extends SocialLoginService {
        private final String code;
        private final List<String> events;
        private SocialProviderProfile profile;
        private Authentication authenticationDuringCodeCreation;

        private RecordingSocialLoginService(String code, List<String> events) {
            super(null, null, null, null, null, null, null);
            this.code = code;
            this.events = events;
        }

        @Override
        public String createOneTimeCode(SocialProviderProfile profile) {
            events.add("create-one-time-code");
            this.profile = profile;
            authenticationDuringCodeCreation = SecurityContextHolder.getContext().getAuthentication();

            return code;
        }
    }
}
