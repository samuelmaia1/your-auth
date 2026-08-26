package com.samuelmaia1_github.yourauth.presentation.controller;

import com.samuelmaia1_github.yourauth.domain.auth.AuthenticatedAccount;
import com.samuelmaia1_github.yourauth.domain.auth.AuthenticatedProjectApiKey;
import com.samuelmaia1_github.yourauth.domain.auth.UserAuthService;
import com.samuelmaia1_github.yourauth.domain.refreshtoken.UserRefreshTokenService;
import com.samuelmaia1_github.yourauth.domain.shared.PageResult;
import com.samuelmaia1_github.yourauth.domain.shared.Pagination;
import com.samuelmaia1_github.yourauth.domain.user.User;
import com.samuelmaia1_github.yourauth.domain.user.UserService;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.user.TokenDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.user.UserLoginDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.user.UserLoginResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.user.CreateUserDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.user.UpdateUserDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.user.UserResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.mapper.UserPresentationMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/projects/{projectId}/users")
public class ProjectUserController {
    private final UserService userService;
    private final UserAuthService userAuthService;
    private final UserRefreshTokenService refreshTokenService;

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
