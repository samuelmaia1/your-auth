package com.samuelmaia1_github.yourauth.domain.usersession.exceptions;

public class UserSessionNotFoundException extends RuntimeException {
    public UserSessionNotFoundException() {
        super("Sessão de usuário não encontrada.");
    }
}
