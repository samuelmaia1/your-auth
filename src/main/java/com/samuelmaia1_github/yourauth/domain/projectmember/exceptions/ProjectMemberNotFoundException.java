package com.samuelmaia1_github.yourauth.domain.projectmember.exceptions;

public class ProjectMemberNotFoundException extends RuntimeException {
    public ProjectMemberNotFoundException() {
        super("Membro do projeto não encontrado.");
    }
}
