package com.samuelmaia1_github.yourauth.presentation.controller;

import com.samuelmaia1_github.yourauth.domain.auth.AuthenticatedProjectApiKey;
import com.samuelmaia1_github.yourauth.domain.projectapikey.exceptions.InvalidProjectApiKeyCredentialsException;
import com.samuelmaia1_github.yourauth.domain.user.User;
import com.samuelmaia1_github.yourauth.domain.user.UserService;
import com.samuelmaia1_github.yourauth.presentation.dto.user.CreateUserDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.user.UserResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.mapper.UserPresentationMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public ResponseEntity<UserResponseDTO> create(
            @AuthenticationPrincipal AuthenticatedProjectApiKey authenticatedApiKey,
            @Valid @RequestBody CreateUserDTO dto
    ) {
        User user = UserPresentationMapper.toDomain(dto, authenticatedApiKey.projectId());
        User createdUser = userService.createWithApiKey(user, authenticatedApiKey.scopes());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(UserPresentationMapper.toResponseDTO(createdUser));
    }
}
