package com.samuelmaia1_github.yourauth.domain.auth.exceptions;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Credenciais inválidas.");
    }

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
