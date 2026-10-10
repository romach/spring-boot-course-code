package com.example.library.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration(proxyBeanMethods = false)
public class OpenApiConfig {

    @Bean
    OpenAPI libraryOpenApi() throws IOException {
        return new OpenAPI()
            .info(new Info()
                .title("Library API")
                .version("v1")
                .description(new ClassPathResource("api-guide.md").getContentAsString(StandardCharsets.UTF_8)))
            .components(ProblemResponses.components());   // the error responses BookApi and AuthorApi refer to
    }
}
