package com.samuelmaia1_github.yourauth.domain.refreshtoken.exceptions;

public class RefreshTokenHashException extends RuntimeException {
    public RefreshTokenHashException() {
        super("Falha ao calcular hash do refresh token");
    }

    public RefreshTokenHashException(String message) {
        super(message);
    }

    public RefreshTokenHashException(String message, Throwable cause) {
        super(message, cause);
    }
}
