package com.samuelmaia1_github.yourauth.presentation.exception;

import com.samuelmaia1_github.yourauth.presentation.dto.error.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException exception) {
        Map<String, String> fields = new LinkedHashMap<>();

        exception.getBindingResult().getFieldErrors().forEach(error ->
                fields.putIfAbsent(error.getField(), getErrorMessage(error.getDefaultMessage()))
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.buildError(
                        HttpStatus.BAD_REQUEST,
                        "Erro de validação nos campos da requisição",
                        fields
                ));
    }

    private String getErrorMessage(String message) {
        if (message == null || message.isBlank()) {
            return "Valor inválido";
        }

        return message;
    }
}
