package com.samuelmaia1_github.yourauth.domain.account.exceptions;

public class AccountAlreadyExistsException extends RuntimeException{
    public AccountAlreadyExistsException() {
        super("Conta já cadastrada");
    }

    public AccountAlreadyExistsException(String message) {
        super(message);
    }
}
