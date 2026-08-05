package com.samuelmaia1_github.yourauth.domain.account.exceptions;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException() {
        super("Conta não encontrada.");
    }

    public AccountNotFoundException(String message) {
        super(message);
    }
}
