package com.samuelmaia1_github.yourauth.domain.auth.exceptions;

public class MaxActiveSessionsExceededException extends RuntimeException {
    public MaxActiveSessionsExceededException(String message) {
        super(message);
    }

    public MaxActiveSessionsExceededException() {
        super("O número máximo de sessões ativas já foi atingido");
    }
}
