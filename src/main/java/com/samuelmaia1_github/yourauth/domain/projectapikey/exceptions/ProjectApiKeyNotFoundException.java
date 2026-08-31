package com.samuelmaia1_github.yourauth.domain.projectapikey.exceptions;

public class ProjectApiKeyNotFoundException extends RuntimeException {
    public ProjectApiKeyNotFoundException() {
        super("API key do projeto não encontrada.");
    }

    public ProjectApiKeyNotFoundException(String message) {
        super(message);
    }
}
