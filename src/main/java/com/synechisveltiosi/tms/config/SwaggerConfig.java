package com.synechisveltiosi.tms.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new io.swagger.v3.oas.models.info.Info()
                        .title("Timesheet Management System API")
                        .version("v1.0.0")
                        .license(new io.swagger.v3.oas.models.info.License().name("Apache 2.0"))
                        .termsOfService("http://swagger.io/terms/")
                        .contact(new io.swagger.v3.oas.models.info.Contact()
                        .name("<NAME>").email("<EMAIL>").url("https://github.com/synechisveltiosi"))
                        .description("Timesheet Management System API"));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("timesheet-management-system-api")
                .pathsToMatch("/api/**")
                .build();
    }
}
