package com.samuelmaia1_github.yourauth.domain.project;

import com.samuelmaia1_github.yourauth.domain.project.exceptions.ProjectAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectPolicy {
    private final ProjectRepository repository;

    public void ensureCanCreate(Project project) {
        if (repository.existsByOwnerAccountIdAndName(project.getOwnerAccountId(), project.getName())) {
            throw new ProjectAlreadyExistsException("Projeto já cadastrado para esta conta");
        }
    }

    public void ensureCanUpdate(Project project) {
        if (repository.existsByOwnerAccountIdAndNameAndIdNot(
                project.getOwnerAccountId(),
                project.getName(),
                project.getId()
        )) {
            throw new ProjectAlreadyExistsException("Projeto já cadastrado para esta conta");
        }
    }
}
