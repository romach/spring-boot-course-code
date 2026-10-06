package com.example.library.book;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import com.example.library.author.Author;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@WebMvcTest(BookController.class)                // only the web layer, and only this controller
class BookControllerTest {

    @Autowired MockMvcTester mvc;                // sends requests through Spring MVC, no server
    @MockitoBean BookService books;              // a Mockito mock, registered as the BookService bean

    Author hunt = new Author("Andrew Hunt");
    Author thomas = new Author("David Thomas");
    Book book = new Book("The Pragmatic Programmer", 1999, Set.of(thomas, hunt));

    @Test
    @DisplayName("GET /api/books/{id} returns the book as JSON, authors sorted by name")
    void byId_found() {
        when(books.getById(book.getId())).thenReturn(book);

        assertThat(mvc.get().uri("/api/books/{id}", book.getId()))
            .hasStatusOk()
            .hasContentType(MediaType.APPLICATION_JSON)
            .bodyJson()                                  // strict: every field, nothing extra, arrays in order
            .isStrictlyEqualTo("""
                {
                  "id": "%s",
                  "title": "The Pragmatic Programmer",
                  "year": 1999,
                  "authors": [
                    { "id": "%s", "name": "Andrew Hunt" },
                    { "id": "%s", "name": "David Thomas" }
                  ]
                }
                """.formatted(book.getId(), hunt.getId(), thomas.getId()));
    }

    @Test
    @DisplayName("GET /api/books/{id} for a missing book is a 404 problem")
    void byId_missing() {
        UUID id = UUID.randomUUID();
        when(books.getById(id)).thenThrow(new NotFoundException("No book with id " + id));

        assertThat(mvc.get().uri("/api/books/{id}", id))
            .hasStatus(HttpStatus.NOT_FOUND)
            .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
            .bodyJson()
            .isStrictlyEqualTo("""
                {
                  "status": 404,
                  "title": "Resource not found",
                  "detail": "No book with id %s",
                  "instance": "/api/books/%s"
                }
                """.formatted(id, id));
    }

    @Test
    @DisplayName("GET /api/books/{id} with a malformed id is a 400 and never reaches the service")
    void byId_malformedId() {
        assertThat(mvc.get().uri("/api/books/{id}", "not-a-uuid"))
            .hasStatus(HttpStatus.BAD_REQUEST)
            .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
            .bodyJson()
            .isStrictlyEqualTo("""
                {
                  "status": 400,
                  "title": "Bad Request",
                  "detail": "Failed to convert 'id' with value: 'not-a-uuid'",
                  "instance": "/api/books/not-a-uuid"
                }
                """);

        verifyNoInteractions(books);                     // the request never got past Spring MVC
    }

    @Test
    @DisplayName("GET /api/books asks for page 0 of 20 by title then id, and returns the stable page shape")
    void all_defaults() {
        Pageable expected = PageRequest.of(0, 20, Sort.by("title", "id"));   // what the controller should ask for
        when(books.findAll(isNull(), any(Pageable.class)))                     // stub loosely: any page request...
            .thenReturn(new PageImpl<>(List.of(book), expected, 1));          // ...gets the page answering `expected`

        assertThat(mvc.get().uri("/api/books"))
            .hasStatusOk()
            .bodyJson()
            .isStrictlyEqualTo("""
                {
                  "content": [
                    {
                      "id": "%s",
                      "title": "The Pragmatic Programmer",
                      "year": 1999,
                      "authors": [
                        { "id": "%s", "name": "Andrew Hunt" },
                        { "id": "%s", "name": "David Thomas" }
                      ]
                    }
                  ],
                  "page": { "size": 20, "number": 0, "totalElements": 1, "totalPages": 1 }
                }
                """.formatted(book.getId(), hunt.getId(), thomas.getId()));

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(books).findAll(isNull(), pageable.capture());                  // ...verify exactly
        assertThat(pageable.getValue()).isEqualTo(expected);
    }

    @Test
    @DisplayName("GET /api/books passes the author filter and the client's sort, id still last")
    void all_filteredAndSorted() {
        UUID authorId = UUID.randomUUID();
        Pageable expected = PageRequest.of(1, 5, Sort.by(Sort.Order.desc("year"), Sort.Order.asc("id")));
        when(books.findAll(eq(authorId), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(), expected, 1));              // one match: page 1 is past the end

        assertThat(mvc.get().uri("/api/books?authorId={a}&page=1&size=5&sort=year,desc", authorId))
            .hasStatusOk()
            .bodyJson()
            .isStrictlyEqualTo("""
                {
                  "content": [],
                  "page": { "size": 5, "number": 1, "totalElements": 1, "totalPages": 1 }
                }
                """);

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(books).findAll(eq(authorId), pageable.capture());
        assertThat(pageable.getValue()).isEqualTo(expected);
    }

    @Test
    @DisplayName("POST /api/books creates the book: 201, Location header, body")
    void create_valid() {
        when(books.create(any(), anyInt(), any())).thenReturn(book);   // stub loosely...

        assertThat(mvc.post().uri("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "title": "The Pragmatic Programmer",
                      "year": 1999,
                      "authorIds": ["%s", "%s"]
                    }
                    """.formatted(hunt.getId(), thomas.getId())))
            .hasStatus(HttpStatus.CREATED)
            .hasHeader("Location", "/api/books/" + book.getId())
            .bodyJson()
            .isStrictlyEqualTo("""
                {
                  "id": "%s",
                  "title": "The Pragmatic Programmer",
                  "year": 1999,
                  "authors": [
                    { "id": "%s", "name": "Andrew Hunt" },
                    { "id": "%s", "name": "David Thomas" }
                  ]
                }
                """.formatted(book.getId(), hunt.getId(), thomas.getId()));

        verify(books).create("The Pragmatic Programmer", 1999, Set.of(hunt.getId(), thomas.getId()));   // ...verify exactly
    }

    @Test
    @DisplayName("POST /api/books with invalid fields is a 400 naming each one, and nothing is created")
    void create_invalid() {
        assertThat(mvc.post().uri("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "title": " ", "year": 0, "authorIds": [] }
                    """))
            .hasStatus(HttpStatus.BAD_REQUEST)
            .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
            .bodyJson()
            .isStrictlyEqualTo("""
                {
                  "status": 400,
                  "title": "Validation failed",
                  "detail": "One or more fields are invalid.",
                  "instance": "/api/books",
                  "errors": {
                    "title": "must not be blank",
                    "year": "must be greater than 0",
                    "authorIds": "must not be empty"
                  }
                }
                """);

        verifyNoInteractions(books);                     // @Valid stopped it before the controller ran
    }

    @Test
    @DisplayName("PUT /api/books/{id} replaces the book and returns it")
    void replace_valid() {
        when(books.replace(any(), any(), anyInt(), any())).thenReturn(book);

        assertThat(mvc.put().uri("/api/books/{id}", book.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "title": "The Pragmatic Programmer",
                      "year": 1999,
                      "authorIds": ["%s", "%s"]
                    }
                    """.formatted(hunt.getId(), thomas.getId())))
            .hasStatusOk()
            .bodyJson()
            .isStrictlyEqualTo("""
                {
                  "id": "%s",
                  "title": "The Pragmatic Programmer",
                  "year": 1999,
                  "authors": [
                    { "id": "%s", "name": "Andrew Hunt" },
                    { "id": "%s", "name": "David Thomas" }
                  ]
                }
                """.formatted(book.getId(), hunt.getId(), thomas.getId()));

        verify(books).replace(book.getId(), "The Pragmatic Programmer", 1999, Set.of(hunt.getId(), thomas.getId()));
    }

    @Test
    @DisplayName("DELETE /api/books/{id} is a 204 with no body")
    void delete_found() {
        assertThat(mvc.delete().uri("/api/books/{id}", book.getId()))
            .hasStatus(HttpStatus.NO_CONTENT)
            .body().isEmpty();

        verify(books).delete(book.getId());
    }

    @Test
    @DisplayName("DELETE /api/books/{id} for a missing book is a 404 problem")
    void delete_missing() {
        UUID id = UUID.randomUUID();
        doThrow(new NotFoundException("No book with id " + id)).when(books).delete(id);   // delete is void

        assertThat(mvc.delete().uri("/api/books/{id}", id))
            .hasStatus(HttpStatus.NOT_FOUND)
            .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
            .bodyJson()
            .isStrictlyEqualTo("""
                {
                  "status": 404,
                  "title": "Resource not found",
                  "detail": "No book with id %s",
                  "instance": "/api/books/%s"
                }
                """.formatted(id, id));
    }
}
