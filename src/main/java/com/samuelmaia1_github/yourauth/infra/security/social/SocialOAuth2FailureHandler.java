package com.samuelmaia1_github.yourauth.infra.security.social;

import com.samuelmaia1_github.yourauth.domain.social.SocialLoginErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class SocialOAuth2FailureHandler implements AuthenticationFailureHandler {
    private static final String ACCESS_DENIED = "access_denied";

    private final SocialLoginRedirectUrlFactory redirectUrlFactory;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException, ServletException {
        SocialLoginErrorCode code = errorCode(exception);

        response.sendRedirect(redirectUrlFactory.error(code));
    }

    private SocialLoginErrorCode errorCode(AuthenticationException exception) {
        if (
                exception instanceof OAuth2AuthenticationException oauth2Exception
                        && ACCESS_DENIED.equals(oauth2Exception.getError().getErrorCode())
        ) {
            return SocialLoginErrorCode.SOCIAL_LOGIN_DENIED;
        }

        return SocialLoginErrorCode.SOCIAL_PROVIDER_ERROR;
    }
}
