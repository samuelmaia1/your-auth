package com.samuelmaia1_github.yourauth.domain.projectapikey.exceptions;

public class ProjectApiKeyAlreadyRevokedException extends RuntimeException {
    public ProjectApiKeyAlreadyRevokedException() {
        super("API key do projeto já revogada.");
    }

    public ProjectApiKeyAlreadyRevokedException(String message) {
        super(message);
    }
}
