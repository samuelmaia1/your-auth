package com.samuelmaia1_github.yourauth.domain.project.exceptions;

public class ProjectAccessDeniedException extends RuntimeException {
    public ProjectAccessDeniedException() {
        super("A conta autenticada não tem permissão para acessar este projeto.");
    }

    public ProjectAccessDeniedException(String message) {
        super(message);
    }
}
