package com.samuelmaia1_github.yourauth.domain.user.exceptions;

public class UserAlreadyExistsException extends RuntimeException{
    public UserAlreadyExistsException() {
        super("Usuário já cadastrado");
    }

    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
