package com.samuelmaia1_github.yourauth.domain.shared.exceptions;

public class IllegalPaginationException extends RuntimeException {
    public IllegalPaginationException() {
        super("Paginação inválida.");
    }

    public IllegalPaginationException(String message) {
        super(message);
    }
}
