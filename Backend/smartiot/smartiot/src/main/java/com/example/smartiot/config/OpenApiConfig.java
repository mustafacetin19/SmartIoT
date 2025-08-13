package com.example.smartiot.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI smartIotOpenAPI() {
        return new OpenAPI()
                // UI'daki Server seçicisinde sabit adresin görünmesi için:
                .addServersItem(new Server().url("http://localhost:8080"))
                .info(new Info()
                        .title("SmartIoT API")
                        .description("Cihaz kontrolü, oda yönetimi ve MQTT entegrasyonu için REST API")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("SmartIoT Team")
                                .email("support@smartiot.local"))
                        .license(new License().name("Apache 2.0")))
                .externalDocs(new ExternalDocumentation()
                        .description("Proje Wiki")
                        .url("https://example.com/wiki"));
    }
}
