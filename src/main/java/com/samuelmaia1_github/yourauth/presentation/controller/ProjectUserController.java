package com.samuelmaia1_github.yourauth.presentation.controller;

import com.samuelmaia1_github.yourauth.domain.auth.AuthenticatedAccount;
import com.samuelmaia1_github.yourauth.domain.shared.PageResult;
import com.samuelmaia1_github.yourauth.domain.shared.Pagination;
import com.samuelmaia1_github.yourauth.domain.user.User;
import com.samuelmaia1_github.yourauth.domain.user.UserService;
import com.samuelmaia1_github.yourauth.presentation.dto.user.CreateUserDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.user.UpdateUserDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.user.UserResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.mapper.UserPresentationMapper;
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

@RequiredArgsConstructor
@RestController
@RequestMapping("/projects/{projectId}/users")
public class ProjectUserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponseDTO> create(
            @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount,
            @PathVariable String projectId,
            @Valid @RequestBody CreateUserDTO dto
    ) {
        User user = UserPresentationMapper.toDomain(dto, projectId);
        User createdUser = userService.create(user, authenticatedAccount.id());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(UserPresentationMapper.toResponseDTO(createdUser));
    }

    @GetMapping
    public ResponseEntity<PageResult<UserResponseDTO>> findAll(
            @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount,
            @PathVariable String projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageResult<User> users = userService.findAllByProjectId(
                projectId,
                authenticatedAccount.id(),
                new Pagination(page, size)
        );

        return ResponseEntity.ok(UserPresentationMapper.toResponseDTO(users));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> findById(
            @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount,
            @PathVariable String projectId,
            @PathVariable String userId
    ) {
        User user = userService.findById(projectId, userId, authenticatedAccount.id());

        return ResponseEntity.ok(UserPresentationMapper.toResponseDTO(user));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> update(
            @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount,
            @PathVariable String projectId,
            @PathVariable String userId,
            @Valid @RequestBody UpdateUserDTO dto
    ) {
        User user = UserPresentationMapper.toDomain(dto);
        User updatedUser = userService.update(projectId, userId, user, authenticatedAccount.id());

        return ResponseEntity.ok(UserPresentationMapper.toResponseDTO(updatedUser));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount,
            @PathVariable String projectId,
            @PathVariable String userId
    ) {
        userService.delete(projectId, userId, authenticatedAccount.id());

        return ResponseEntity.noContent().build();
    }
}
