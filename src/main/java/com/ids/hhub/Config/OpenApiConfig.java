package com.ids.hhub.Config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "basicAuth";

        return new OpenAPI()
                // 1. Definisci le Info del progetto
                .info(new Info()
                        .title("HackHub API")
                        .version("1.0")
                        .description("API Documentation for HackHub Project"))

                // 2. Aggiungi il requisito di sicurezza globale
                // (Dice a Swagger: "Applica questo schema a tutte le chiamate")
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))

                // 3. Definisci CHE TIPO di sicurezza usi (Basic Auth)
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("basic")));
    }
}