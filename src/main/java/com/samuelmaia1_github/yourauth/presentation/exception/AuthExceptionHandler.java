package com.samuelmaia1_github.yourauth.presentation.exception;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.samuelmaia1_github.yourauth.domain.auth.exceptions.*;
import com.samuelmaia1_github.yourauth.domain.refreshtoken.exceptions.ExpiredRefreshTokenException;
import com.samuelmaia1_github.yourauth.domain.refreshtoken.exceptions.RefreshTokenHashException;
import com.samuelmaia1_github.yourauth.domain.refreshtoken.exceptions.RefreshTokenReuseException;
import com.samuelmaia1_github.yourauth.presentation.dto.error.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AuthExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException exception) {
        return buildError(HttpStatus.UNAUTHORIZED, exception.getMessage());
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidToken(InvalidTokenException exception) {
        return buildError(HttpStatus.UNAUTHORIZED, exception.getMessage());
    }

    @ExceptionHandler(ExpiredRefreshTokenException.class)
    public ResponseEntity<ErrorResponse> handleExpiredRefreshToken(ExpiredRefreshTokenException exception) {
        return buildError(HttpStatus.UNAUTHORIZED, exception.getMessage());
    }

    @ExceptionHandler(RefreshTokenReuseException.class)
    public ResponseEntity<ErrorResponse> handleRefreshTokenReuse(RefreshTokenReuseException exception) {
        return buildError(HttpStatus.UNAUTHORIZED, exception.getMessage());
    }

    @ExceptionHandler(JWTVerificationException.class)
    public ResponseEntity<ErrorResponse> handleJwtVerification(JWTVerificationException exception) {
        return buildError(HttpStatus.UNAUTHORIZED, exception.getMessage());
    }

    @ExceptionHandler(GenerateTokenFailException.class)
    public ResponseEntity<ErrorResponse> handleGenerateTokenFail(GenerateTokenFailException exception) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
    }

    @ExceptionHandler(RefreshTokenHashException.class)
    public ResponseEntity<ErrorResponse> handleRefreshTokenHash(RefreshTokenHashException exception) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
    }

    @ExceptionHandler(LoginBlockedException.class)
    public ResponseEntity<ErrorResponse> handleLoginBlocked(LoginBlockedException exception) {
        return buildError(HttpStatus.UNAUTHORIZED, exception.getMessage());
    }

    @ExceptionHandler(MaxActiveSessionsExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxActiveSessionsExceeded(MaxActiveSessionsExceededException exception) {
        return buildError(HttpStatus.CONFLICT, exception.getMessage());
    }

    private ResponseEntity<ErrorResponse> buildError(HttpStatus status, String message) {
        return ResponseEntity
                .status(status)
                .body(ErrorResponse.buildError(status, message));
    }
}
