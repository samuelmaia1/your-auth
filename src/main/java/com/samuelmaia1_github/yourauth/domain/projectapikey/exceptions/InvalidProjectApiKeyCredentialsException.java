package com.samuelmaia1_github.yourauth.domain.projectapikey.exceptions;

public class InvalidProjectApiKeyCredentialsException extends RuntimeException {
    public InvalidProjectApiKeyCredentialsException() {
        super("API key inválida.");
    }

    public InvalidProjectApiKeyCredentialsException(String message) {
        super(message);
    }
}
