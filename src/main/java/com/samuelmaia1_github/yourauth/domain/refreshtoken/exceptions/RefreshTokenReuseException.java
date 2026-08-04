package com.samuelmaia1_github.yourauth.domain.refreshtoken.exceptions;

public class RefreshTokenReuseException extends RuntimeException {
    public RefreshTokenReuseException() {
        super("Token de rotação já utilizado. Sessão encerrada.");
    }

    public RefreshTokenReuseException(String message) {
        super(message);
    }
}
