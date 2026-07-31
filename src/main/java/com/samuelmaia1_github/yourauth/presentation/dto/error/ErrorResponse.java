package com.samuelmaia1_github.yourauth.presentation.dto.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private String message;
    private Integer status;
    private String error;
    private LocalDateTime time;
    private Map<String, String> fields;

    private ErrorResponse(
            String message,
            Integer status,
            String error,
            LocalDateTime time,
            Map<String, String> fields
    ) {
        this.message = message;
        this.status = status;
        this.error = error;
        this.time = time;
        this.fields = fields;
    }

    public static ErrorResponse buildError(HttpStatus status, String message) {
        return new ErrorResponse(
                message,
                status.value(),
                status.getReasonPhrase(),
                LocalDateTime.now(),
                null
        );
    }

    public static ErrorResponse buildError(
            HttpStatus status,
            String message,
            Map<String, String> fields
    ) {
        return new ErrorResponse(
                message,
                status.value(),
                status.getReasonPhrase(),
                LocalDateTime.now(),
                fields
        );
    }
}