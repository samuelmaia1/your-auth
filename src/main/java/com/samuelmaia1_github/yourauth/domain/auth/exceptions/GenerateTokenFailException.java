package com.samuelmaia1_github.yourauth.domain.auth.exceptions;

public class GenerateTokenFailException extends RuntimeException {
    public GenerateTokenFailException() {
        super("Erro ao gerar token JWT");
    }

    public GenerateTokenFailException(String message) {
        super(message);
    }
}
