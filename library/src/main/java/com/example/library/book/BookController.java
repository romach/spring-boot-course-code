package com.example.library.book;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService books;

    public BookController(BookService books) {
        this.books = books;
    }

    @GetMapping                                          // GET /api/books  or  ?authorId=...
    public List<BookResponse> all(@RequestParam(required = false) UUID authorId) {
        return books.findAll(authorId).stream().map(BookResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> byId(@PathVariable UUID id) {
        return books.findById(id)
            .map(BookResponse::from)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping                                         // POST /api/books
    public ResponseEntity<BookResponse> create(@RequestBody CreateBookRequest req) {
        Book saved = books.create(req.title(), req.year(), req.authorIds());
        URI location = URI.create("/api/books/" + saved.getId());
        return ResponseEntity.created(location).body(BookResponse.from(saved));
    }

    @PutMapping("/{id}")                                 // PUT /api/books/{id}
    public ResponseEntity<BookResponse> replace(@PathVariable UUID id, @RequestBody CreateBookRequest req) {
        return books.replace(id, req.title(), req.year(), req.authorIds())
            .map(BookResponse::from)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")                     // DELETE /api/books/{id}
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        return books.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
