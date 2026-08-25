package com.samuelmaia1_github.yourauth.domain.projectapikey.exceptions;

public class ProjectApiKeyAccessDeniedException extends RuntimeException {
    public ProjectApiKeyAccessDeniedException() {
        super("A API key não tem permissão para executar esta operação.");
    }

    public ProjectApiKeyAccessDeniedException(String message) {
        super(message);
    }
}
