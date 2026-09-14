package com.samuelmaia1_github.yourauth.infra.security;

import com.samuelmaia1_github.yourauth.domain.auth.AuthenticatedAccount;
import com.samuelmaia1_github.yourauth.domain.auth.AuthenticatedProjectApiKey;
import com.samuelmaia1_github.yourauth.domain.auth.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SecurityFilter extends OncePerRequestFilter {
    private final TokenService tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var token = tokenService.recoverToken(request);
        var currentAuthentication = SecurityContextHolder.getContext().getAuthentication();

        if (isAuthenticatedAsAccount(currentAuthentication)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (isAuthenticatedAsProjectApiKey(currentAuthentication)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!tokenService.isValidAccountAccessToken(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        var subject = new AuthenticatedAccount(
                tokenService.getSubject(token),
                tokenService.getEmail(token),
                tokenService.getSessionId(token)
        );

        var auth = new UsernamePasswordAuthenticationToken(
                subject,
                null,
                List.of()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }

    private boolean isAuthenticatedAsAccount(Authentication authentication) {
        return authentication != null
                && authentication.getPrincipal() instanceof AuthenticatedAccount;
    }

    private boolean isAuthenticatedAsProjectApiKey(Authentication authentication) {
        return authentication != null
                && authentication.getPrincipal() instanceof AuthenticatedProjectApiKey;
    }
}
