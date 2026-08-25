package com.samuelmaia1_github.yourauth.presentation.exception;

import com.samuelmaia1_github.yourauth.domain.projectapikey.exceptions.InvalidProjectApiKeyException;
import com.samuelmaia1_github.yourauth.domain.projectapikey.exceptions.InvalidProjectApiKeyCredentialsException;
import com.samuelmaia1_github.yourauth.domain.projectapikey.exceptions.ExpiredProjectApiKeyException;
import com.samuelmaia1_github.yourauth.domain.projectapikey.exceptions.ProjectApiKeyAccessDeniedException;
import com.samuelmaia1_github.yourauth.domain.projectapikey.exceptions.ProjectApiKeyAlreadyRevokedException;
import com.samuelmaia1_github.yourauth.domain.projectapikey.exceptions.ProjectApiKeyHashException;
import com.samuelmaia1_github.yourauth.domain.projectapikey.exceptions.ProjectApiKeyNotFoundException;
import com.samuelmaia1_github.yourauth.domain.projectapikey.exceptions.RevokedProjectApiKeyException;
import com.samuelmaia1_github.yourauth.presentation.dto.error.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ProjectApiKeyExceptionHandler {
    @ExceptionHandler(InvalidProjectApiKeyException.class)
    public ResponseEntity<ErrorResponse> handleInvalidProjectApiKey(InvalidProjectApiKeyException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.buildError(HttpStatus.BAD_REQUEST, exception.getMessage()));
    }

    @ExceptionHandler(InvalidProjectApiKeyCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidProjectApiKeyCredentials(InvalidProjectApiKeyCredentialsException exception) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.buildError(HttpStatus.UNAUTHORIZED, exception.getMessage()));
    }

    @ExceptionHandler(ExpiredProjectApiKeyException.class)
    public ResponseEntity<ErrorResponse> handleExpiredProjectApiKey(ExpiredProjectApiKeyException exception) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.buildError(HttpStatus.UNAUTHORIZED, exception.getMessage()));
    }

    @ExceptionHandler(RevokedProjectApiKeyException.class)
    public ResponseEntity<ErrorResponse> handleRevokedProjectApiKey(RevokedProjectApiKeyException exception) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.buildError(HttpStatus.UNAUTHORIZED, exception.getMessage()));
    }

    @ExceptionHandler(ProjectApiKeyAccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleProjectApiKeyAccessDenied(ProjectApiKeyAccessDeniedException exception) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.buildError(HttpStatus.FORBIDDEN, exception.getMessage()));
    }

    @ExceptionHandler(ProjectApiKeyNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProjectApiKeyNotFound(ProjectApiKeyNotFoundException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.buildError(HttpStatus.NOT_FOUND, exception.getMessage()));
    }

    @ExceptionHandler(ProjectApiKeyAlreadyRevokedException.class)
    public ResponseEntity<ErrorResponse> handleProjectApiKeyAlreadyRevoked(ProjectApiKeyAlreadyRevokedException exception) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.buildError(HttpStatus.CONFLICT, exception.getMessage()));
    }

    @ExceptionHandler(ProjectApiKeyHashException.class)
    public ResponseEntity<ErrorResponse> handleProjectApiKeyHash(ProjectApiKeyHashException exception) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.buildError(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Falha ao processar API key do projeto."
                ));
    }
}
