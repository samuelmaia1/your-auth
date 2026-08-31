package com.samuelmaia1_github.yourauth.infra.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.time.LocalDateTime;

public class SecurityErrorResponseWriter {
    private SecurityErrorResponseWriter() {
    }

    public static void write(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("""
                {"message":"%s","status":%d,"error":"%s","time":"%s"}
                """.formatted(
                escape(message),
                status.value(),
                status.getReasonPhrase(),
                LocalDateTime.now()
        ));
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }

        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
