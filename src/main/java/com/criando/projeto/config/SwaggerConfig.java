package com.criando.projeto.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI publicApi() {
        return new OpenAPI().info(
                new Info()
                        .title("Wallet Connect Orchestrator")
                        .version("v1.0.0")
                        .description("Serviços de criação e gerenciamento de autorizações de carteira do PagBank")
        );
    }
}