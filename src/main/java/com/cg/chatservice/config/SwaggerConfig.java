package com.cg.chatservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    private static final String API_KEY_SCHEME = "X-API-KEY";
    private static final String USER_ID_SCHEME = "X-User-Id";
    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local Development Server")
                ))
                .addSecurityItem(new SecurityRequirement()
                        .addList(API_KEY_SCHEME)
                        .addList(USER_ID_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(API_KEY_SCHEME,
                                new SecurityScheme()
                                        .name(API_KEY_SCHEME)
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                                        .description("API Key — default: my-local-dev-api-key-12345")
                        )
                        .addSecuritySchemes(USER_ID_SCHEME,
                                new SecurityScheme()
                                        .name(USER_ID_SCHEME)
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                                        .description("User ID — example: user-001")
                        )
                );
    }

    /**
     * Adds X-User-Id as a visible input field on every endpoint in Swagger UI.
     */
    @Bean
    public OperationCustomizer globalHeaderCustomizer() {
        return (operation, handlerMethod) -> {
            operation.addParametersItem(
                    new Parameter()
                            .in("header")
                            .name("X-User-Id")
                            .description("ID of the requesting user")
                            .required(true)
                            .schema(new StringSchema().example("user-001"))
            );
            return operation;
        };
    }

    private Info apiInfo() {
        return new Info()
                .title("Chat Service API")
                .description("""
                        ## Chat Session & Message Management Service
                        
                        ### Authentication
                        Click the 🔒 **Authorize** button and enter:
                        - **X-API-KEY** : `my-local-dev-api-key-12345`
                        - **X-User-Id** : `user-001`
                        
                        ### Features
                        - Create and manage chat sessions
                        - Add messages (USER / ASSISTANT / SYSTEM)
                        - Rename and favourite sessions
                        - Paginated message history
                        - Redis caching + Rate limiting
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("Chat Service Team")
                        .email("support@chatservice.com"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }
}