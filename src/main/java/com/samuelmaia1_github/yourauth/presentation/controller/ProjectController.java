package com.samuelmaia1_github.yourauth.presentation.controller;

import com.samuelmaia1_github.yourauth.domain.auth.AuthenticatedAccount;
import com.samuelmaia1_github.yourauth.domain.project.Project;
import com.samuelmaia1_github.yourauth.domain.project.ProjectService;
import com.samuelmaia1_github.yourauth.domain.shared.PageResult;
import com.samuelmaia1_github.yourauth.domain.shared.Pagination;
import com.samuelmaia1_github.yourauth.presentation.dto.project.CreateProjectDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.project.ProjectResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.project.UpdateProjectDTO;
import com.samuelmaia1_github.yourauth.presentation.mapper.ProjectPresentationMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService service;

    @PostMapping("/create")
    public ResponseEntity<ProjectResponseDTO> create(
            @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount,
            @Valid @RequestBody CreateProjectDTO dto
    ) {
        Project project = ProjectPresentationMapper.toDomain(dto, authenticatedAccount.id());
        Project createdProject = service.create(project);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ProjectPresentationMapper.toResponseDTO(createdProject));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponseDTO> findById(
            @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount,
            @PathVariable String id
    ) {
        Project project = service.findById(id, authenticatedAccount.id());

        return ResponseEntity.ok(ProjectPresentationMapper.toResponseDTO(project));
    }

    @GetMapping
    public ResponseEntity<PageResult<ProjectResponseDTO>> findAll(
            @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageResult<Project> projects = service.findAllByAccountId(
                authenticatedAccount.id(),
                new Pagination(page, size)
        );

        return ResponseEntity.ok(ProjectPresentationMapper.toResponseDTO(projects));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponseDTO> update(
            @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount,
            @PathVariable String id,
            @Valid @RequestBody UpdateProjectDTO dto
    ) {
        Project project = ProjectPresentationMapper.toDomain(dto);
        Project updatedProject = service.update(id, project, authenticatedAccount.id());

        return ResponseEntity.ok(ProjectPresentationMapper.toResponseDTO(updatedProject));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount,
            @PathVariable String id
    ) {
        service.delete(id, authenticatedAccount.id());

        return ResponseEntity.noContent().build();
    }
}
