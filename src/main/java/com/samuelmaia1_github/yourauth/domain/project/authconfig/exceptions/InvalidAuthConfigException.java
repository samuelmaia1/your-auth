package com.samuelmaia1_github.yourauth.domain.project.authconfig.exceptions;

public class InvalidAuthConfigException extends RuntimeException {
    public InvalidAuthConfigException() {
        super("Configuração de autenticação inválida.");
    }

    public InvalidAuthConfigException(String message) {
        super(message);
    }
}
