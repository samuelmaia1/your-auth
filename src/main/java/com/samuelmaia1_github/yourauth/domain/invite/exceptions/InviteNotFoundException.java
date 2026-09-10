package com.samuelmaia1_github.yourauth.domain.invite.exceptions;

public class InviteNotFoundException extends RuntimeException {
    public InviteNotFoundException() {
        super("Convite não encontrado.");
    }
}
