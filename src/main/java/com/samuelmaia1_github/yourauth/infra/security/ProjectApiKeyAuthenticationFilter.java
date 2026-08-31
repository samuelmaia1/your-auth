package com.samuelmaia1_github.yourauth.infra.security;

import com.samuelmaia1_github.yourauth.domain.auth.AuthenticatedProjectApiKey;
import com.samuelmaia1_github.yourauth.domain.projectapikey.ProjectApiKeyAuthenticationService;
import com.samuelmaia1_github.yourauth.domain.projectapikey.exceptions.ExpiredProjectApiKeyException;
import com.samuelmaia1_github.yourauth.domain.projectapikey.exceptions.InvalidProjectApiKeyCredentialsException;
import com.samuelmaia1_github.yourauth.domain.projectapikey.exceptions.ProjectApiKeyHashException;
import com.samuelmaia1_github.yourauth.domain.projectapikey.exceptions.RevokedProjectApiKeyException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ProjectApiKeyAuthenticationFilter extends OncePerRequestFilter {
    private static final String API_KEY_PREFIX = "ya_sk_";
    private static final String API_KEY_HEADER = "X-API-Key";

    private final ProjectApiKeyAuthenticationService authenticationService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String rawApiKey = recoverApiKey(request);

        if (rawApiKey == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            AuthenticatedProjectApiKey authenticatedApiKey = authenticationService.authenticate(rawApiKey);
            var auth = new UsernamePasswordAuthenticationToken(
                    authenticatedApiKey,
                    null,
                    List.of()
            );
            SecurityContextHolder.getContext().setAuthentication(auth);

            filterChain.doFilter(request, response);
        } catch (
                InvalidProjectApiKeyCredentialsException
                | ExpiredProjectApiKeyException
                | RevokedProjectApiKeyException exception
        ) {
            SecurityErrorResponseWriter.write(response, HttpStatus.UNAUTHORIZED, exception.getMessage());
        } catch (ProjectApiKeyHashException exception) {
            SecurityErrorResponseWriter.write(
                    response,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Falha ao processar API key do projeto."
            );
        }
    }

    private String recoverApiKey(HttpServletRequest request) {
        String apiKey = request.getHeader(API_KEY_HEADER);

        if (apiKey != null && !apiKey.isBlank()) {
            return apiKey.trim();
        }

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.replace("Bearer ", "").trim();

            if (token.startsWith(API_KEY_PREFIX)) {
                return token;
            }
        }

        return null;
    }
}
