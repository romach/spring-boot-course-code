package com.example.library;

import java.util.UUID;
import com.example.library.author.AuthorRepository;
import com.example.library.author.AuthorResponse;
import com.example.library.book.BookRepository;
import com.example.library.book.BookResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.client.EntityExchangeResult;
import org.springframework.test.web.servlet.client.RestTestClient;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)   // the whole app, on a real server and a free port
@AutoConfigureRestTestClient                                   // a RestTestClient pointed at that server
@Import(TestcontainersConfiguration.class)                     // ...and a real PostgreSQL
class LibraryApiIntegrationTest {

    @Autowired RestTestClient client;          // sends real HTTP requests to the running app
    @Autowired BookRepository books;           // used only to empty the tables
    @Autowired AuthorRepository authors;

    @BeforeEach
    void emptyTables() {
        books.deleteAll();                     // books first: their book_author rows point at the authors
        authors.deleteAll();
    }

    @Test
    @DisplayName("a book created with POST can be read back from its Location")
    void create_thenReadBack() {
        UUID hunt = createAuthor("Andrew Hunt");
        UUID thomas = createAuthor("David Thomas");

        EntityExchangeResult<BookResponse> created = client.post().uri("/api/books")
            .contentType(MediaType.APPLICATION_JSON)
            .body("""
                {
                  "title": "The Pragmatic Programmer",
                  "year": 1999,
                  "authorIds": ["%s", "%s"]
                }
                """.formatted(hunt, thomas))
            .exchange()
            .expectStatus().isCreated()
            .expectBody(BookResponse.class)
            .returnResult();
        UUID id = created.getResponseBody().id();

        client.get().uri(created.getResponseHeaders().getLocation())   // follow the Location header
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody().json("""
                {
                  "id": "%s",
                  "title": "The Pragmatic Programmer",
                  "year": 1999,
                  "authors": [
                    { "id": "%s", "name": "Andrew Hunt" },
                    { "id": "%s", "name": "David Thomas" }
                  ]
                }
                """.formatted(id, hunt, thomas), JsonCompareMode.STRICT);
    }

    @Test
    @DisplayName("GET /api/books lists every book in title order, with the page metadata")
    void list_allBooksByTitle() {
        UUID hunt = createAuthor("Andrew Hunt");
        UUID thomas = createAuthor("David Thomas");
        UUID evans = createAuthor("Eric Evans");
        UUID pragmatic = createBook("""
            { "title": "The Pragmatic Programmer", "year": 1999, "authorIds": ["%s", "%s"] }
            """.formatted(hunt, thomas));
        UUID ddd = createBook("""
            { "title": "Domain-Driven Design", "year": 2003, "authorIds": ["%s"] }
            """.formatted(evans));

        client.get().uri("/api/books")
            .exchange()
            .expectStatus().isOk()
            .expectBody().json("""
                {
                  "content": [
                    {
                      "id": "%s",
                      "title": "Domain-Driven Design",
                      "year": 2003,
                      "authors": [
                        { "id": "%s", "name": "Eric Evans" }
                      ]
                    },
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
                  "page": { "size": 20, "number": 0, "totalElements": 2, "totalPages": 1 }
                }
                """.formatted(ddd, evans, pragmatic, hunt, thomas), JsonCompareMode.STRICT);
    }

    @Test
    @DisplayName("DELETE of an author who still has books is a 409, and the author stays")
    void deleteAuthor_withBooks_conflict() {
        UUID evans = createAuthor("Eric Evans");
        createBook("""
            { "title": "Domain-Driven Design", "year": 2003, "authorIds": ["%s"] }
            """.formatted(evans));

        client.delete().uri("/api/authors/{id}", evans)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.CONFLICT)
            .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .expectBody().json("""
                {
                  "status": 409,
                  "title": "Conflict",
                  "detail": "Author %s still has books; delete or re-author them first",
                  "instance": "/api/authors/%s"
                }
                """.formatted(evans, evans), JsonCompareMode.STRICT);

        client.get().uri("/api/authors/{id}", evans)   // the refused delete changed nothing
            .exchange()
            .expectStatus().isOk()
            .expectBody().json("""
                { "id": "%s", "name": "Eric Evans" }
                """.formatted(evans), JsonCompareMode.STRICT);
    }

    // POSTs an author and returns the id the API gave it.
    UUID createAuthor(String name) {
        return client.post().uri("/api/authors")
            .contentType(MediaType.APPLICATION_JSON)
            .body("""
                { "name": "%s" }
                """.formatted(name))
            .exchange()
            .expectStatus().isCreated()
            .expectBody(AuthorResponse.class)
            .returnResult()
            .getResponseBody()
            .id();
    }

    // POSTs a book (the request body as JSON) and returns the id the API gave it.
    UUID createBook(String json) {
        return client.post().uri("/api/books")
            .contentType(MediaType.APPLICATION_JSON)
            .body(json)
            .exchange()
            .expectStatus().isCreated()
            .expectBody(BookResponse.class)
            .returnResult()
            .getResponseBody()
            .id();
    }
}
