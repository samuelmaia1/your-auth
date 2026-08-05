package com.samuelmaia1_github.yourauth.infra.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.samuelmaia1_github.yourauth.domain.auth.AuthenticatedUser;
import com.samuelmaia1_github.yourauth.domain.auth.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.List;

@Component
public class SecurityFilter extends OncePerRequestFilter {
    private final TokenService tokenService;
    private final HandlerExceptionResolver handlerExceptionResolver;

    public SecurityFilter(
            TokenService tokenService,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver handlerExceptionResolver
    ) {
        this.tokenService = tokenService;
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var token = tokenService.recoverToken(request);

        try {
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                if (token != null && tokenService.isValid(token)) {
                    var subject = new AuthenticatedUser(tokenService.getSubject(token), tokenService.getEmail(token));

                    var auth = new UsernamePasswordAuthenticationToken(
                            subject,
                            null,
                            List.of()
                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        } catch (JWTVerificationException exception) {
            SecurityContextHolder.clearContext();
            handlerExceptionResolver.resolveException(request, response, null, exception);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
