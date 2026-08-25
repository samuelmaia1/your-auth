package com.samuelmaia1_github.yourauth.domain.projectapikey.exceptions;

public class RevokedProjectApiKeyException extends RuntimeException {
    public RevokedProjectApiKeyException() {
        super("API key revogada.");
    }

    public RevokedProjectApiKeyException(String message) {
        super(message);
    }
}
