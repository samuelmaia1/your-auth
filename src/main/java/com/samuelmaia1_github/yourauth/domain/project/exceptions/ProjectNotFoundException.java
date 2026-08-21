package com.samuelmaia1_github.yourauth.domain.project.exceptions;

public class ProjectNotFoundException extends RuntimeException {
    public ProjectNotFoundException() {
        super("Projeto não encontrado.");
    }

    public ProjectNotFoundException(String message) {
        super(message);
    }
}
