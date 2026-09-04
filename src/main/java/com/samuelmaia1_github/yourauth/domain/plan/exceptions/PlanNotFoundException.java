package com.samuelmaia1_github.yourauth.domain.plan.exceptions;

public class PlanNotFoundException extends RuntimeException {
    public PlanNotFoundException() {
        super("Plano não encontrado.");
    }
}
