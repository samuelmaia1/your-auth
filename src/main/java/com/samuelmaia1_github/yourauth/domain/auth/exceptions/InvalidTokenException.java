package com.samuelmaia1_github.yourauth.domain.auth.exceptions;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException() {
        super("Token inválido ou expirado");
    }

    public InvalidTokenException(String message) {
        super(message);
    }
}
