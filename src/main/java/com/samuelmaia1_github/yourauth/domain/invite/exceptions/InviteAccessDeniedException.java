package com.samuelmaia1_github.yourauth.domain.invite.exceptions;

public class InviteAccessDeniedException extends RuntimeException {
    public InviteAccessDeniedException() {
        super("A conta autenticada não tem permissão para acessar este convite.");
    }
}
