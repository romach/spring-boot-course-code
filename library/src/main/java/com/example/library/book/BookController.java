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

    @GetMapping                                          // GET /api/books  or  /api/books?author=...
    public List<Book> all(@RequestParam(required = false) String author) {
        return books.findAll(author);                    // books = the BookService
    }

    @GetMapping("/{id}")                        // GET /api/books/{id}
    public ResponseEntity<Book> byId(@PathVariable UUID id) {
        return books.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping                                // POST /api/books
    public ResponseEntity<Book> create(@RequestBody Book incoming) {
        Book saved = books.create(incoming);
        URI location = URI.create("/api/books/" + saved.getId());   // was saved.id()
        return ResponseEntity.created(location).body(saved);
    }

    @PutMapping("/{id}")                        // PUT /api/books/{id}
    public ResponseEntity<Book> replace(@PathVariable UUID id, @RequestBody Book incoming) {
        return books.replace(id, incoming)
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
