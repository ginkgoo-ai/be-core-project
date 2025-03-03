package com.ginkgooai.core.project.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Value("${core-gateway-public-uri}")
    private String gatewayUri;

    private static final String COOKIE_AUTH_NAME = "cookieAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .servers(List.of(new Server().url(gatewayUri + "/api/project")))
                .info(new Info().title("Project Service API").version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList(COOKIE_AUTH_NAME))
                .components(new Components()
                        .addSecuritySchemes(COOKIE_AUTH_NAME, new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name("SESSION")
                                .description("Session cookie for authentication"))
                        .addParameters("workspaceId", new io.swagger.v3.oas.models.parameters.Parameter()
                                .in("header")
                                .name("x-workspace-id")
                                .description("Workspace ID for multi-tenant requests")
                                .required(true)
                                .schema(new io.swagger.v3.oas.models.media.Schema<>().type("string"))));
    }
}
