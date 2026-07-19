package com.feelingpilates.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI feelingPilatesOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("FeelingPilates API")
                        .description("API de FeelingPilates")
                        .version("v1"));
    }
}
