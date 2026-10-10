package com.example.library;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.client.RestTestClient;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)   // the same three annotations as LibraryApiIntegrationTest,
@AutoConfigureRestTestClient                                   // so both classes share one cached context
@Import(TestcontainersConfiguration.class)
class ApiDocsIntegrationTest {

    @Autowired RestTestClient client;

    @Test
    @DisplayName("/v3/api-docs serves the spec, with what the code alone can't say")
    void apiDocs() {
        client.get().uri("/v3/api-docs")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.info.title").isEqualTo("Library API")
            .jsonPath("$.paths['/api/books'].post.responses['201']").exists()
            .jsonPath("$.paths['/api/books'].get.parameters[*].name").isEqualTo(List.of("authorId", "page", "size", "sort"))
            .jsonPath("$.paths['/api/authors/{id}'].delete.responses['409']['$ref']").isEqualTo("#/components/responses/AuthorHasBooks")
            .jsonPath("$.components.responses.AuthorHasBooks.content['application/problem+json'].examples").exists();
    }
}
