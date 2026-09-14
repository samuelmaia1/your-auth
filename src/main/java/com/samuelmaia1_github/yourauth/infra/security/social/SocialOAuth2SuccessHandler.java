package com.samuelmaia1_github.yourauth.infra.security.social;

import com.samuelmaia1_github.yourauth.domain.social.SocialLoginErrorCode;
import com.samuelmaia1_github.yourauth.domain.social.SocialLoginService;
import com.samuelmaia1_github.yourauth.domain.social.SocialProviderProfile;
import com.samuelmaia1_github.yourauth.domain.social.exceptions.SocialLoginException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class SocialOAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final SocialOAuth2ProfileResolver profileResolver;
    private final SocialLoginService socialLoginService;
    private final SocialLoginRedirectUrlFactory redirectUrlFactory;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        try {
            if (!(authentication instanceof OAuth2AuthenticationToken oauth2Authentication)) {
                throw new SocialLoginException(SocialLoginErrorCode.SOCIAL_LOGIN_FAILED);
            }

            SocialProviderProfile profile = profileResolver.resolve(oauth2Authentication);
            String code = socialLoginService.createOneTimeCode(profile);
            String redirectUrl = redirectUrlFactory.success(code);

            clearTemporaryOAuthAuthentication(request);
            response.sendRedirect(redirectUrl);
        } catch (SocialLoginException exception) {
            response.sendRedirect(redirectUrlFactory.error(exception.getCode()));
        } catch (RuntimeException exception) {
            response.sendRedirect(redirectUrlFactory.error(SocialLoginErrorCode.SOCIAL_LOGIN_FAILED));
        }
    }

    private void clearTemporaryOAuthAuthentication(HttpServletRequest request) {
        SecurityContextHolder.clearContext();

        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }

        session.removeAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        session.invalidate();
    }
}
