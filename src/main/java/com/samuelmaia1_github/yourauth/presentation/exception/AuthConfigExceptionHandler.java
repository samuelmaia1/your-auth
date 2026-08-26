package com.samuelmaia1_github.yourauth.presentation.exception;

import com.samuelmaia1_github.yourauth.domain.project.authconfig.exceptions.AuthConfigNotFoundException;
import com.samuelmaia1_github.yourauth.domain.project.authconfig.exceptions.InvalidAuthConfigException;
import com.samuelmaia1_github.yourauth.presentation.dto.error.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AuthConfigExceptionHandler {
    @ExceptionHandler(AuthConfigNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAuthConfigNotFound(AuthConfigNotFoundException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.buildError(HttpStatus.NOT_FOUND, exception.getMessage()));
    }

    @ExceptionHandler(InvalidAuthConfigException.class)
    public ResponseEntity<ErrorResponse> handleInvalidAuthConfig(InvalidAuthConfigException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.buildError(HttpStatus.BAD_REQUEST, exception.getMessage()));
    }
}
