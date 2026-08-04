package com.samuelmaia1_github.yourauth.domain.refreshtoken.exceptions;

public class ExpiredRefreshTokenException extends RuntimeException {
    public ExpiredRefreshTokenException() {
        super("Token de rotação expirado");
    }

    public ExpiredRefreshTokenException(String message) {
        super(message);
    }
}
