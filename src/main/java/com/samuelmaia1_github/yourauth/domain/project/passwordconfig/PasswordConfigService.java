package com.samuelmaia1_github.yourauth.domain.project.passwordconfig;

import com.samuelmaia1_github.yourauth.domain.project.exceptions.ProjectNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PasswordConfigService {
    private final PasswordConfigRepository repository;

    public PasswordConfig findByProjectId(String projectId) {
        return repository
                .findByProjectId(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Projeto não encontrado com id: " + projectId));
    }
}
