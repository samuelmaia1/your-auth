package com.samuelmaia1_github.yourauth.domain.plan.exceptions;

public class PlanLimitExceededException extends RuntimeException {
    public PlanLimitExceededException(String message) {
        super(message);
    }
}
