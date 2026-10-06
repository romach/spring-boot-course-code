package com.example.library.author;

import java.util.UUID;
import com.example.library.error.ConflictException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@WebMvcTest(AuthorController.class)
class AuthorControllerTest {

    @Autowired MockMvcTester mvc;
    @MockitoBean AuthorService authors;

    Author hunt = new Author("Andrew Hunt");

    @Test
    @DisplayName("GET /api/authors/{id} returns the author as JSON")
    void byId_found() {
        when(authors.getById(hunt.getId())).thenReturn(hunt);

        assertThat(mvc.get().uri("/api/authors/{id}", hunt.getId()))
            .hasStatusOk()
            .bodyJson()
            .isStrictlyEqualTo("""
                { "id": "%s", "name": "Andrew Hunt" }
                """.formatted(hunt.getId()));
    }

    @Test
    @DisplayName("POST /api/authors creates the author: 201, Location header, body")
    void create_valid() {
        when(authors.create(any())).thenReturn(hunt);

        assertThat(mvc.post().uri("/api/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "name": "Andrew Hunt" }
                    """))
            .hasStatus(HttpStatus.CREATED)
            .hasHeader("Location", "/api/authors/" + hunt.getId())
            .bodyJson()
            .isStrictlyEqualTo("""
                { "id": "%s", "name": "Andrew Hunt" }
                """.formatted(hunt.getId()));

        verify(authors).create("Andrew Hunt");
    }

    @Test
    @DisplayName("POST /api/authors with a blank name is a 400, and nothing is created")
    void create_blankName() {
        assertThat(mvc.post().uri("/api/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "name": "" }
                    """))
            .hasStatus(HttpStatus.BAD_REQUEST)
            .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
            .bodyJson()
            .isStrictlyEqualTo("""
                {
                  "status": 400,
                  "title": "Validation failed",
                  "detail": "One or more fields are invalid.",
                  "instance": "/api/authors",
                  "errors": { "name": "must not be blank" }
                }
                """);

        verifyNoInteractions(authors);
    }

    @Test
    @DisplayName("DELETE /api/authors/{id} for an author who still has books is a 409 problem")
    void delete_stillHasBooks() {
        UUID id = UUID.randomUUID();
        doThrow(new ConflictException("Author " + id + " still has books; delete or re-author them first"))
            .when(authors).delete(id);

        assertThat(mvc.delete().uri("/api/authors/{id}", id))
            .hasStatus(HttpStatus.CONFLICT)
            .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
            .bodyJson()
            .isStrictlyEqualTo("""
                {
                  "status": 409,
                  "title": "Conflict",
                  "detail": "Author %s still has books; delete or re-author them first",
                  "instance": "/api/authors/%s"
                }
                """.formatted(id, id));
    }
}
