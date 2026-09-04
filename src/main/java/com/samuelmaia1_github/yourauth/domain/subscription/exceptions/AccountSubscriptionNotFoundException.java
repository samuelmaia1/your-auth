package com.samuelmaia1_github.yourauth.domain.subscription.exceptions;

public class AccountSubscriptionNotFoundException extends RuntimeException {
    public AccountSubscriptionNotFoundException() {
        super("Assinatura da conta não encontrada.");
    }
}
