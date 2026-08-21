package com.samuelmaia1_github.yourauth.domain.project.exceptions;

public class ProjectAlreadyExistsException extends RuntimeException {
    public ProjectAlreadyExistsException() {
        super("Projeto já cadastrado.");
    }

    public ProjectAlreadyExistsException(String message) {
        super(message);
    }
}
