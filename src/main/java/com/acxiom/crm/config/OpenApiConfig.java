package com.acxiom.crm.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AcxiomCRM REST API")
                        .version("1.0.0")
                        .description("Official REST API endpoints for Customer, Lead, and Opportunity Management in AcxiomCRM.")
                        .contact(new Contact().name("Technical Assessment Evaluator").email("admin@acxiom.com"))
                        .license(new License().name("Apache 2.0")));
    }
}
