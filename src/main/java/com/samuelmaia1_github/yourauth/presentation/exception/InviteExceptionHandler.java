package com.samuelmaia1_github.yourauth.presentation.exception;

import com.samuelmaia1_github.yourauth.domain.invite.exceptions.InviteAccessDeniedException;
import com.samuelmaia1_github.yourauth.domain.invite.exceptions.InviteConflictException;
import com.samuelmaia1_github.yourauth.domain.invite.exceptions.InviteInvalidException;
import com.samuelmaia1_github.yourauth.domain.invite.exceptions.InviteNotFoundException;
import com.samuelmaia1_github.yourauth.presentation.dto.error.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class InviteExceptionHandler {
    @ExceptionHandler(InviteNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleInviteNotFound(InviteNotFoundException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.buildError(HttpStatus.NOT_FOUND, exception.getMessage()));
    }

    @ExceptionHandler(InviteAccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleInviteAccessDenied(InviteAccessDeniedException exception) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.buildError(HttpStatus.FORBIDDEN, exception.getMessage()));
    }

    @ExceptionHandler(InviteInvalidException.class)
    public ResponseEntity<ErrorResponse> handleInviteInvalid(InviteInvalidException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.buildError(HttpStatus.BAD_REQUEST, exception.getMessage()));
    }

    @ExceptionHandler(InviteConflictException.class)
    public ResponseEntity<ErrorResponse> handleInviteConflict(InviteConflictException exception) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.buildError(HttpStatus.CONFLICT, exception.getMessage()));
    }
}
