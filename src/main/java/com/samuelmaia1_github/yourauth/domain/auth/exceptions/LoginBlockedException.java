package com.samuelmaia1_github.yourauth.domain.auth.exceptions;

public class LoginBlockedException extends RuntimeException{
    public LoginBlockedException() {
        super("Login bloqueado.");
    }

    public LoginBlockedException(String message) {
        super(message);
    }
}
