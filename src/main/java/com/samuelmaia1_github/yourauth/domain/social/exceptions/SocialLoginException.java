package com.samuelmaia1_github.yourauth.domain.social.exceptions;

import com.samuelmaia1_github.yourauth.domain.social.SocialLoginErrorCode;

public class SocialLoginException extends RuntimeException {
    private final SocialLoginErrorCode code;

    public SocialLoginException(SocialLoginErrorCode code) {
        super(code.name());
        this.code = code;
    }

    public SocialLoginException(SocialLoginErrorCode code, Throwable cause) {
        super(code.name(), cause);
        this.code = code;
    }

    public SocialLoginErrorCode getCode() {
        return code;
    }
}
