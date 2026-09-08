package com.samuelmaia1_github.yourauth.domain.plan.exceptions;

public class PlanLimitNotConfiguredException extends RuntimeException {
    public PlanLimitNotConfiguredException(String message) {
        super(message);
    }
}
