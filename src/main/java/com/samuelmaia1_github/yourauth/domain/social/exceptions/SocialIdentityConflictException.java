package com.samuelmaia1_github.yourauth.domain.social.exceptions;

public class SocialIdentityConflictException extends RuntimeException {
    public SocialIdentityConflictException() {
        super("Identidade social já vinculada.");
    }
}
