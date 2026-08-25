package com.samuelmaia1_github.yourauth.infra.config;

import com.samuelmaia1_github.yourauth.infra.security.SecurityFilter;
import com.samuelmaia1_github.yourauth.infra.security.ProjectApiKeyAuthenticationFilter;
import com.samuelmaia1_github.yourauth.infra.security.SecurityErrorResponseWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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
                            .requestMatchers(HttpMethod.POST, "/accounts/create").permitAll()
                            .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                            .requestMatchers(HttpMethod.POST, "/auth/refresh").permitAll()
                            .anyRequest().authenticated()
                )
                .addFilterBefore(projectApiKeyAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class);

        return security.build();
    }
}
