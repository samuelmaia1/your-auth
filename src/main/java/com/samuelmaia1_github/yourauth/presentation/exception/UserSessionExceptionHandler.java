package com.samuelmaia1_github.yourauth.presentation.exception;

import com.samuelmaia1_github.yourauth.domain.usersession.exceptions.UserSessionNotFoundException;
import com.samuelmaia1_github.yourauth.presentation.dto.error.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class UserSessionExceptionHandler {
    @ExceptionHandler(UserSessionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserSessionNotFound(UserSessionNotFoundException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.buildError(HttpStatus.NOT_FOUND, exception.getMessage()));
    }
}
