package com.samuelmaia1_github.yourauth.domain.projectapikey.exceptions;

public class InvalidProjectApiKeyException extends RuntimeException {
    public InvalidProjectApiKeyException() {
        super("API key do projeto inválida.");
    }

    public InvalidProjectApiKeyException(String message) {
        super(message);
    }
}
