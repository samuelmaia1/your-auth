package com.samuelmaia1_github.yourauth.infra.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI yourAuthOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Your Auth API")
                        .version("0.0.1-SNAPSHOT")
                        .description("API de autenticacao e identidade para contas proprietarias, projetos, usuarios finais, sessoes, refresh tokens e API keys de projeto."))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Ambiente local")
                ))
                .components(new Components()
                        .addSecuritySchemes(
                                "bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT de conta proprietaria enviado no header Authorization: Bearer <token>.")
                        )
                        .addSecuritySchemes(
                                "projectApiKey",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                                        .name("X-API-Key")
                                        .description("API key de projeto usada por clientes terceiros. Tambem pode ser enviada como Authorization: Bearer ya_sk_...")
                        )
                        .addSecuritySchemes(
                                "accessTokenCookie",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.COOKIE)
                                        .name("access-token")
                                        .description("Cookie HTTP-only usado pelo fluxo web para autenticacao.")
                        )
                        .addSecuritySchemes(
                                "refreshTokenCookie",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.COOKIE)
                                        .name("refresh_token")
                                        .description("Cookie HTTP-only usado pelo fluxo web para renovar a sessao.")
                        ));
    }
}
