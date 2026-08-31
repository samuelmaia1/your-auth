package com.samuelmaia1_github.yourauth.domain.projectapikey.exceptions;

public class ExpiredProjectApiKeyException extends RuntimeException {
    public ExpiredProjectApiKeyException() {
        super("API key expirada.");
    }

    public ExpiredProjectApiKeyException(String message) {
        super(message);
    }
}
