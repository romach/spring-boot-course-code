package com.example.library.author;

import java.util.List;
import java.util.UUID;
import com.example.library.error.ConflictException;
import com.example.library.error.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    @DisplayName("GET /api/authors/{id} for a missing author is a 404 problem")
    void byId_missing() {
        UUID id = UUID.randomUUID();
        when(authors.getById(id)).thenThrow(new NotFoundException("No author with id " + id));

        assertThat(mvc.get().uri("/api/authors/{id}", id))
            .hasStatus(HttpStatus.NOT_FOUND)
            .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
            .bodyJson()
            .isStrictlyEqualTo("""
                {
                  "status": 404,
                  "title": "Resource not found",
                  "detail": "No author with id %s",
                  "instance": "/api/authors/%s"
                }
                """.formatted(id, id));
    }

    @Test
    @DisplayName("GET /api/authors/{id} with a malformed id is a 400 and never reaches the service")
    void byId_malformedId() {
        assertThat(mvc.get().uri("/api/authors/{id}", "not-a-uuid"))
            .hasStatus(HttpStatus.BAD_REQUEST)
            .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
            .bodyJson()
            .isStrictlyEqualTo("""
                {
                  "status": 400,
                  "title": "Bad Request",
                  "detail": "Failed to convert 'id' with value: 'not-a-uuid'",
                  "instance": "/api/authors/not-a-uuid"
                }
                """);

        verifyNoInteractions(authors);
    }

    @Test
    @DisplayName("GET /api/authors asks for page 0 of 20 by name then id, and returns the stable page shape")
    void all_defaults() {
        Pageable expected = PageRequest.of(0, 20, Sort.by("name", "id"));
        when(authors.findAll(any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(hunt), expected, 1));

        assertThat(mvc.get().uri("/api/authors"))
            .hasStatusOk()
            .bodyJson()
            .isStrictlyEqualTo("""
                {
                  "content": [
                    { "id": "%s", "name": "Andrew Hunt" }
                  ],
                  "page": { "size": 20, "number": 0, "totalElements": 1, "totalPages": 1 }
                }
                """.formatted(hunt.getId()));

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(authors).findAll(pageable.capture());
        assertThat(pageable.getValue()).isEqualTo(expected);
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
    @DisplayName("PUT /api/authors/{id} replaces the author and returns it")
    void replace_valid() {
        when(authors.replace(any(), any())).thenReturn(hunt);

        assertThat(mvc.put().uri("/api/authors/{id}", hunt.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "name": "Andrew Hunt" }
                    """))
            .hasStatusOk()
            .bodyJson()
            .isStrictlyEqualTo("""
                { "id": "%s", "name": "Andrew Hunt" }
                """.formatted(hunt.getId()));

        verify(authors).replace(hunt.getId(), "Andrew Hunt");
    }

    @Test
    @DisplayName("PUT /api/authors/{id} with a blank name is a 400, and nothing is replaced")
    void replace_blankName() {
        assertThat(mvc.put().uri("/api/authors/{id}", hunt.getId())
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
                  "instance": "/api/authors/%s",
                  "errors": { "name": "must not be blank" }
                }
                """.formatted(hunt.getId()));

        verifyNoInteractions(authors);
    }

    @Test
    @DisplayName("PUT /api/authors/{id} for a missing author is a 404 problem")
    void replace_missing() {
        UUID id = UUID.randomUUID();
        when(authors.replace(id, "Andrew Hunt")).thenThrow(new NotFoundException("No author with id " + id));

        assertThat(mvc.put().uri("/api/authors/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "name": "Andrew Hunt" }
                    """))
            .hasStatus(HttpStatus.NOT_FOUND)
            .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
            .bodyJson()
            .isStrictlyEqualTo("""
                {
                  "status": 404,
                  "title": "Resource not found",
                  "detail": "No author with id %s",
                  "instance": "/api/authors/%s"
                }
                """.formatted(id, id));
    }

    @Test
    @DisplayName("DELETE /api/authors/{id} is a 204 with no body")
    void delete_found() {
        assertThat(mvc.delete().uri("/api/authors/{id}", hunt.getId()))
            .hasStatus(HttpStatus.NO_CONTENT)
            .body().isEmpty();

        verify(authors).delete(hunt.getId());
    }

    @Test
    @DisplayName("DELETE /api/authors/{id} for a missing author is a 404 problem")
    void delete_missing() {
        UUID id = UUID.randomUUID();
        doThrow(new NotFoundException("No author with id " + id)).when(authors).delete(id);

        assertThat(mvc.delete().uri("/api/authors/{id}", id))
            .hasStatus(HttpStatus.NOT_FOUND)
            .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
            .bodyJson()
            .isStrictlyEqualTo("""
                {
                  "status": 404,
                  "title": "Resource not found",
                  "detail": "No author with id %s",
                  "instance": "/api/authors/%s"
                }
                """.formatted(id, id));
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
