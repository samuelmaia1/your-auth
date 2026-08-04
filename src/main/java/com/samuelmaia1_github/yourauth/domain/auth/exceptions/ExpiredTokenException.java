package com.samuelmaia1_github.yourauth.domain.auth.exceptions;

public class ExpiredTokenException extends RuntimeException {
    public ExpiredTokenException() {
        super("Token expirado");
    }

    public ExpiredTokenException(String message) {
        super(message);
    }
}
