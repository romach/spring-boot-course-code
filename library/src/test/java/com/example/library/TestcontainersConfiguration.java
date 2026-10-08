package com.example.library;

import java.util.TimeZone;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;

@TestConfiguration(proxyBeanMethods = false)   // test-only beans, picked up only where a test imports them
public class TestcontainersConfiguration {

    static {
        // Tests don't run LibraryApplication.main(), so they set the same UTC default here.
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Bean
    @ServiceConnection                         // Spring Boot connects the DataSource to this container
    PostgreSQLContainer postgres() {
        return new PostgreSQLContainer("postgres:18");   // the same image as compose.yaml
    }
}
