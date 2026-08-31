package com.samuelmaia1_github.yourauth.domain.project.authconfig.exceptions;

public class AuthConfigNotFoundException extends RuntimeException {
    public AuthConfigNotFoundException() {
        super("Configuração de autenticação não encontrada.");
    }

    public AuthConfigNotFoundException(String message) {
        super(message);
    }
}
