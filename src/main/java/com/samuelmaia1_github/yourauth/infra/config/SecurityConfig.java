package com.samuelmaia1_github.yourauth.infra.config;

import com.samuelmaia1_github.yourauth.domain.auth.AuthenticatedProjectApiKey;
import com.samuelmaia1_github.yourauth.infra.security.SecurityFilter;
import com.samuelmaia1_github.yourauth.infra.security.ProjectApiKeyAuthenticationFilter;
import com.samuelmaia1_github.yourauth.infra.security.SecurityErrorResponseWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.function.Supplier;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final SecurityFilter securityFilter;
    private final ProjectApiKeyAuthenticationFilter projectApiKeyAuthenticationFilter;

    public SecurityConfig(
            SecurityFilter securityFilter,
            ProjectApiKeyAuthenticationFilter projectApiKeyAuthenticationFilter
    ) {
        this.securityFilter = securityFilter;
        this.projectApiKeyAuthenticationFilter = projectApiKeyAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity security) throws Exception {
        security
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint((request, response, exception) ->
                                SecurityErrorResponseWriter.write(
                                        response,
                                        HttpStatus.UNAUTHORIZED,
                                        "Autenticação obrigatória."
                                )
                        )
                        .accessDeniedHandler((request, response, exception) ->
                                SecurityErrorResponseWriter.write(
                                        response,
                                        HttpStatus.FORBIDDEN,
                                        "Acesso negado."
                                )
                        )
                )
                .authorizeHttpRequests(auth ->
                    auth.requestMatchers("/h2-console/**").permitAll()
                            .requestMatchers("/error").permitAll()
                            .requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
                            .requestMatchers(HttpMethod.POST, "/accounts/create").permitAll()
                            .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                            .requestMatchers(HttpMethod.POST, "/auth/refresh").permitAll()
                            .requestMatchers(HttpMethod.POST, "/users/refresh", "/users/logout")
                            .access(SecurityConfig::hasProjectApiKey)
                            .anyRequest().authenticated()
                )
                .addFilterBefore(projectApiKeyAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class);

        return security.build();
    }

    private static AuthorizationDecision hasProjectApiKey(
            Supplier<? extends Authentication> authentication,
            RequestAuthorizationContext context
    ) {
        Authentication currentAuthentication = authentication.get();
        boolean hasProjectApiKey = currentAuthentication != null
                && currentAuthentication.isAuthenticated()
                && !(currentAuthentication instanceof AnonymousAuthenticationToken)
                && currentAuthentication.getPrincipal() instanceof AuthenticatedProjectApiKey;

        return new AuthorizationDecision(hasProjectApiKey);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:*",
                "https://localhost:*",
                "http://127.0.0.1:*",
                "https://127.0.0.1:*",
                "http://[::1]:*",
                "https://[::1]:*"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
