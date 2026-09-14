package com.samuelmaia1_github.yourauth.infra.security.social;

import com.samuelmaia1_github.yourauth.domain.social.SocialLoginErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class SocialLoginRedirectUrlFactory {
    private final String callbackUrl;

    public SocialLoginRedirectUrlFactory(
            @Value("${app.frontend.social-callback-url:http://localhost:3000/auth/callback}")
            String callbackUrl
    ) {
        this.callbackUrl = callbackUrl;
    }

    public String success(String code) {
        return UriComponentsBuilder
                .fromUriString(callbackUrl)
                .queryParam("code", code)
                .build()
                .toUriString();
    }

    public String error(SocialLoginErrorCode code) {
        return UriComponentsBuilder
                .fromUriString(callbackUrl)
                .queryParam("error", code.name())
                .build()
                .toUriString();
    }
}
